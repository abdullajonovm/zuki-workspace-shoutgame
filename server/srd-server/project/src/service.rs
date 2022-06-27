use conf::{SrdServerConf};

use futures::{Async, Poll};
use futures::future::{self, Future};
use futures::stream::Stream;

use hyper::{self, Chunk, Method, StatusCode};
use hyper::header::{AccessControlAllowCredentials,
                    AccessControlAllowHeaders,
                    AccessControlAllowMethods,
                    AccessControlAllowOrigin,
                    AccessControlMaxAge,
                    ContentType,
                    Origin};
use hyper::server::{Request, Response, Service};

use qstring::QString;

use regex::RegexSet;

use siphasher::sip::SipHasher13;

use std::hash::Hasher;
use std::mem;
use std::string::ToString;
use std::sync::Arc;

use unicase::Ascii;


pub struct SrdHttpService {
    server_conf: Arc<SrdServerConf>,
    cors_origin_regex: Arc<RegexSet>,
}

impl SrdHttpService {
    pub fn new(server_conf: SrdServerConf, cors_origin_patterns: Vec<String>) -> SrdHttpService {
        let cors_origin_regex = match RegexSet::new(&*cors_origin_patterns) {
            Ok(set) => set,
            Err(e) => panic!("Error in CORS patterns: {}", e)
        };
        SrdHttpService {
            server_conf: Arc::new(server_conf),
            cors_origin_regex: Arc::new(cors_origin_regex),
        }
    }

    pub fn set_conf(&mut self, server_conf: SrdServerConf) {
        self.server_conf = Arc::new(server_conf);
    }
}

impl Service for SrdHttpService {
    type Request = Request;
    type Response = Response;
    type Error = hyper::Error;
    type Future = SrdHttpResponse;

    fn call(&self, req: Request) -> Self::Future {
        SrdHttpResponse::new(req, Arc::clone(&self.server_conf), Arc::clone(&self.cors_origin_regex))
    }
}

impl Clone for SrdHttpService {
    fn clone(&self) -> SrdHttpService {
        SrdHttpService {
            server_conf: Arc::clone(&self.server_conf),
            cors_origin_regex: self.cors_origin_regex.clone(),
        }
    }
}


enum SrdHttpResponseState {
    NewRequest(Request),
    ReadBody(RequestDetails, Box<Future<Item=Vec<Chunk>, Error=hyper::Error>>),
    ProcessRequest(RequestDetails, Option<String>),
    SendResponse(Response),
    None,
}

impl SrdHttpResponseState {
    fn take(&mut self) -> SrdHttpResponseState {
        mem::replace(self, SrdHttpResponseState::None)
    }
}

enum PollAction<NextStateT> {
    Continue(NextStateT),
    Break(NextStateT),
}

pub struct SrdHttpResponse {
    state: SrdHttpResponseState,
    server_conf: Arc<SrdServerConf>,
    cors_origin_regex: Arc<RegexSet>,
}

impl Future for SrdHttpResponse {
    type Item = Response;
    type Error = hyper::Error;

    fn poll(&mut self) -> Poll<Self::Item, Self::Error> {
        loop {
            let result = match self.state.take() {
                SrdHttpResponseState::NewRequest(request) =>
                    self.new_request(request),
                SrdHttpResponseState::ReadBody(request, read_future) =>
                    self.read_body(request, read_future),
                SrdHttpResponseState::ProcessRequest(request, query_string) =>
                    self.process_request(request, query_string),
                SrdHttpResponseState::SendResponse(response) =>
                    return Ok(Async::Ready(response)),
                SrdHttpResponseState::None =>
                    panic!("Illegal state error")
            };
            match result {
                PollAction::Continue(next_state) =>
                    self.state = next_state,
                PollAction::Break(next_state) => {
                    self.state = next_state;
                    return Ok(Async::NotReady);
                },
            }
        }
    }
}

struct RequestDetails {
    origin_url_str: Option<String>,
}

impl SrdHttpResponse {
    fn new(request: Request, server_conf: Arc<SrdServerConf>, cors_origin_regex: Arc<RegexSet>) -> SrdHttpResponse {
        SrdHttpResponse {
            state: SrdHttpResponseState::NewRequest(request),
            server_conf: server_conf,
            cors_origin_regex: cors_origin_regex,
        }
    }

    fn new_request(&self, request: Request) -> PollAction<SrdHttpResponseState> {
        match request.path() {
            "/srd3.json" => {
                let mut request_details = RequestDetails {
                    origin_url_str: None
                };
                if request.headers().has::<Origin>() {
                    let origin: &Origin = request.headers().get().unwrap();
                    request_details.origin_url_str = origin_to_url_str(origin);
                }
                match request.method() {
                    &Method::Get => {
                        let query_string = request.query().map(|qs| qs.to_string());
                        PollAction::Continue(SrdHttpResponseState::ProcessRequest(request_details, query_string))
                    },
                    &Method::Post => {
                        let mut body_byte_count = 0;
                        let read_body_future = request.body()
                            .take_while(move |chunk| {
                                let result = future::ok(body_byte_count < 1024);
                                body_byte_count += chunk.len();
                                result
                            })
                            .collect();
                        PollAction::Continue(SrdHttpResponseState::ReadBody(request_details, Box::new(read_body_future)))
                    },
                    &Method::Options => {
                        if request.headers().has::<Origin>() {
                            let origin: &Origin = request.headers().get().unwrap();
                            if let Some(origin_url_str) = origin_to_url_str(origin) {
                                if self.cors_origin_regex.is_match(&*origin_url_str) {
                                    return PollAction::Continue(SrdHttpResponseState::SendResponse(
                                        Response::new()
                                            .with_status(StatusCode::Ok)
                                            .with_header(ContentType::plaintext())
                                            .with_header(AccessControlAllowOrigin::Value(origin_url_str))
                                            .with_header(AccessControlAllowCredentials)
                                            .with_header(AccessControlMaxAge(1728000))
                                            .with_header(AccessControlAllowMethods(
                                                vec!(Method::Get, Method::Post, Method::Options)))
                                            .with_header(AccessControlAllowHeaders(
                                                vec!(Ascii::new("Authorization".to_string()),
                                                     Ascii::new("Content-Type".to_string()),
                                                     Ascii::new("Accept".to_string()),
                                                     Ascii::new("Origin".to_string()),
                                                     Ascii::new("User-Agent".to_string()),
                                                     Ascii::new("DNT".to_string()),
                                                     Ascii::new("Cache-Control".to_string()),
                                                     Ascii::new("X-Mx-ReqToken".to_string()),
                                                     Ascii::new("Keep-Alive".to_string()),
                                                     Ascii::new("X-Requested-With".to_string()),
                                                     Ascii::new("If-Modified-Since".to_string()))))
                                    ));
                                }
                            }
                        }
                        PollAction::Continue(SrdHttpResponseState::SendResponse(
                            Response::new().with_status(StatusCode::NotFound)
                        ))
                    },
                    _ => {
                        PollAction::Continue(SrdHttpResponseState::SendResponse(
                            Response::new().with_status(StatusCode::NotFound)
                        ))
                    }
                }
            },
            _ => {
                PollAction::Continue(SrdHttpResponseState::SendResponse(
                    Response::new().with_status(StatusCode::NotFound)
                ))
            }
        }
    }

    fn read_body(&self, request: RequestDetails, mut read_body_future: Box<Future<Item=Vec<Chunk>, Error=hyper::Error>>)
    -> PollAction<SrdHttpResponseState> {
        match read_body_future.poll() {
            Ok(Async::Ready(chunks)) => {
                let bytes_vec = chunks.into_iter()
                    .flat_map(|chunk| chunk.into_iter())
                    .collect();
                match String::from_utf8(bytes_vec) {
                    Ok(query_string) =>
                        PollAction::Continue(SrdHttpResponseState::ProcessRequest(request, Some(query_string))),
                    Err(e) => {
                        println!("Error parsing body: {}", e);
                        PollAction::Continue(SrdHttpResponseState::SendResponse(
                            Response::new().with_status(StatusCode::BadRequest)
                        ))
                    }
                }
            },
            Ok(Async::NotReady) =>
                PollAction::Break(SrdHttpResponseState::ReadBody(request, read_body_future)),
            Err(e) => {
                println!("Error reading body: {}", e);
                PollAction::Continue(SrdHttpResponseState::SendResponse(
                    Response::new().with_status(StatusCode::BadRequest)
                ))
            }
        }
    }

    fn process_request(&self, request: RequestDetails, query_string: Option<String>) -> PollAction<SrdHttpResponseState> {
        match query_string {
            Some(query_string) => {
                trace!("Got query string: {}", query_string);
                let query_string = QString::from(&*query_string);
                match query_string.get("client_id") {
                    Some(client_id) => {
                        let mut hasher = SipHasher13::new();
                        hasher.write(client_id.as_bytes());
                        let client_hash_val = hasher.finish();
                        match self.server_conf.client_json(client_hash_val) {
                            Ok(client_json) => {
                                let mut response = Response::new()
                                    .with_header(ContentType::json())
                                    .with_body(client_json)
                                    .with_status(StatusCode::Ok);
                                if let Some(origin_url_str) = request.origin_url_str {
                                    if self.cors_origin_regex.is_match(&*origin_url_str) {
                                        response = response
                                            .with_header(AccessControlAllowOrigin::Value(origin_url_str))
                                            .with_header(AccessControlAllowCredentials)
                                    }
                                }
                                PollAction::Continue(SrdHttpResponseState::SendResponse(response))
                            },
                            Err(e) => {
                                println!("Error serializing client json: {}", e);
                                PollAction::Continue(SrdHttpResponseState::SendResponse(
                                    Response::new()
                                        .with_status(StatusCode::InternalServerError)
                                ))
                            }
                        }
                    },
                    None => {
                        PollAction::Continue(SrdHttpResponseState::SendResponse(
                            Response::new()
                                .with_body("missing client id parameter")
                                .with_status(StatusCode::BadRequest)
                        ))
                    }
                }
            },
            None => {
                warn!("Got no query string");
                PollAction::Continue(SrdHttpResponseState::SendResponse(
                    Response::new()
                        .with_body("missing client id parameter")
                        .with_status(StatusCode::BadRequest)
                ))
            }
        }
    }
}

fn origin_to_url_str(origin: &Origin) -> Option<String> {
    if let (Some(scheme), Some(host)) = (origin.scheme(), origin.host()) {
        let port = match host.port() {
            Some(port) => format!(":{}", port),
            None => String::new()
        };
        return Some(format!("{}://{}{}", scheme, host.hostname(), port));
    }
    None
}