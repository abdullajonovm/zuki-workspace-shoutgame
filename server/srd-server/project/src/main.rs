extern crate chan as chans;
extern crate chan_signal;
extern crate chrono;
extern crate core_affinity;
extern crate crossbeam_channel;
extern crate fern;
extern crate futures;
extern crate hyper;
#[macro_use]
extern crate log;
extern crate log_panics;
extern crate mio;
extern crate net2;
extern crate num_cpus;
extern crate qstring;
extern crate regex;
extern crate serde;
#[macro_use]
extern crate serde_derive;
extern crate serde_json;
extern crate siphasher;
extern crate structopt;
#[macro_use]
extern crate structopt_derive;
extern crate thread_control;
extern crate tokio_core;
extern crate unicase;

extern crate native_tls;
extern crate tokio_tls;

mod chan;
mod conf;
mod dispatcher;
mod listener;
mod service;
mod token;
mod worker;

use conf::SrdServerConf;

use dispatcher::ConnectionDispatcher;

use native_tls::{TlsAcceptor, Pkcs12};

use service::SrdHttpService;

use std::cmp::max;
use std::fs::File;
use std::io::prelude::*;
use std::net::SocketAddr;
use std::string::ToString;
use std::sync::Arc;
use std::thread;

use structopt::StructOpt;
use structopt::clap;


#[derive(StructOpt)]
struct SrdServerOpts {
    #[structopt(short="v", long="verbose", help="Outputs verbose logging")]
    verbose: bool,

    #[structopt(short="t", long="threads",
                help="The number of worker threads to respond to requests. Defaults to autodetect.")]
    threads: Option<u16>,

    #[structopt(short="n", long="network", default_value="127.0.0.1",
                help="The network to listen on")]
    network: String,

    #[structopt(long="backlog", default_value="100",
                help="The maximum number of TCP connections to allow to be backlogged at any time.")]
    tcp_backlog: u16,

    #[structopt(long="cors-origins", default_value="https?://.*",
                help="Comma-delimited list of regex patterns. If one matches an Origin request header, the corresponding response will include CORS authorization.")]
    cors_origin_patterns: String,

    //TODO #[structopt(long="worker-limit", default_value="10",
    //TODO             help="The maximum number of TCP connections to allow each worker to process at the same time.")]
    //TODO worker_limit: Option<u16>,

    #[structopt(long="default-conf", default_value="/etc/srd-server/default.conf",
                help="The path to the default configuration file")]
    default_conf_path: String,

    #[structopt(long="servers-conf", default_value="/etc/srd-server/servers.conf",
                help="The path to the servers configuration file")]
    servers_conf_path: String,

    #[structopt(short="p", long="http",
                help="The local port to service HTTP requests")]
    http_port: Option<u16>,

    #[structopt(long="tokens",
                help="The number of tokens to create and distribute among server endpoints", default_value="128")]
    tokens: u16,

    #[structopt(long="reuse-port",
                help="Each worker thread will bind to the socket, eliminating the need for a dedicated listener thread")]
    reuse_port: bool,

    #[structopt(short="P", long="https",
                help="The local port to service HTTPS requests. Requires keycert argument.")]
    https_port: Option<u16>,

    #[structopt(short="k", long="keycert", requires="https_port",
                help="The PKCS#12 file containing the TLS key and certificates. Required if HTTPS port is set.")]
    p12_file: Option<String>,

    //TODO:  this is insecure. accept the name of an environment variable or use stdin
    #[structopt(short="x", long="keycertpass", requires="p12_file",
                help="The password for the PKCS#12 file")]
    p12_file_pass: Option<String>,
}

fn init_logger(verbose: bool) {
    let global_level = if verbose { log::LevelFilter::Debug } else { log::LevelFilter::Info };
    let global_dispatch = fern::Dispatch::new()
        .format(|out, message, record| {
            out.finish(format_args!(
                "{} {} [{}] {}",
                chrono::Local::now().format("%m/%d %H:%M:%S%.3f"),
                record.level(),
                record.target(),
                message
            ))
        })
        .level(global_level)
        .chain(std::io::stdout());
    
    if let Err(e) = global_dispatch.apply() {
        panic!("Error initializing logging: {}", e);
    }

    log_panics::init()
}

fn main() {
    let opts = SrdServerOpts::from_args();

    init_logger(opts.verbose);

    let num_threads = match opts.threads {
        Some(t) =>
            t as usize,
        None =>
            if opts.reuse_port {
                num_cpus::get()
            } else {
                // one thread is dedicated to accepting connections
                max(1, num_cpus::get() - 1)
            }
    };

    if let (None, None) = (opts.http_port, opts.https_port) {
        clap::Error::with_description("At least one of HTTP or HTTPS port argument must be provided",
                                      clap::ErrorKind::MissingRequiredArgument)
                    .exit();
    }

    // Signal channel must be initialized before any other thread
    let signal_channel = chan_signal::notify(&[chan_signal::Signal::HUP,
                                               chan_signal::Signal::INT,
                                               chan_signal::Signal::TERM]);
    
    let token_count = opts.tokens;

    let srd_server_conf = SrdServerConf::read_conf_files(opts.default_conf_path.clone(),
                                                         opts.servers_conf_path.clone(),
                                                         token_count);

    let cors_origin_patterns = opts.cors_origin_patterns.split(',')
        .map(ToString::to_string)
        .collect();
    let srd_http_service = SrdHttpService::new(srd_server_conf, cors_origin_patterns);

    let mut dispatchers = Vec::new();

    if let Some(port) = opts.http_port {
        let http_address = format!("{}:{}", opts.network, port);
        let http_address = match http_address.parse::<SocketAddr>() {
            Ok(addr) => addr,
            Err(err) => panic!("Invalid network and/or http_port: {}", err)
        };
        let dispatcher = ConnectionDispatcher::spawn("http",
                                                     http_address,
                                                     opts.reuse_port,
                                                     None,
                                                     num_threads,
                                                     opts.tcp_backlog,
                                                     srd_http_service.clone());
        dispatchers.push(dispatcher);
    }

    if let Some(p12_file_path) = opts.p12_file {
        let mut p12_file = File::open(p12_file_path).expect("Error opening PKCS#12 archive");
        let mut p12_data = Vec::new();
        p12_file.read_to_end(&mut p12_data).expect("Error reading PKCS#12 archive");

        let pkcs12_pass = match opts.p12_file_pass {
            Some(pass) => pass,
            None => String::new()
        };
        let pkcs12 = Pkcs12::from_der(&p12_data[..], &pkcs12_pass).expect("Error reading PKCS#12 file");

        let tls_acceptor_builder = TlsAcceptor::builder(pkcs12).expect("Error preparing TLS acceptor");
        //tls_acceptor_builder.supported_protocols(&[Sslv3, Tlsv10, Tlsv11, Tlsv12]);
        let tls_acceptor = tls_acceptor_builder.build().expect("Error building TLS acceptor");

        let https_address = format!("{}:{}", opts.network, opts.https_port.unwrap());
        let https_address = match https_address.parse::<SocketAddr>() {
            Ok(addr) => addr,
            Err(err) => panic!("Invalid network and/or https_port: {}", err)
        };

        let dispatcher = ConnectionDispatcher::spawn("https",
                                                     https_address,
                                                     opts.reuse_port,
                                                     Some(tls_acceptor),
                                                     num_threads,
                                                     opts.tcp_backlog,
                                                     srd_http_service.clone());
        dispatchers.push(dispatcher);
    }

    spawn_signal_listener(signal_channel,
                          dispatchers.clone(),
                          opts.default_conf_path,
                          opts.servers_conf_path,
                          token_count,
                          srd_http_service);

    dispatchers.into_iter()
        .for_each(|dispatcher| {
            dispatcher.wait();
        });
}

fn spawn_signal_listener(signal_channel: chans::Receiver<chan_signal::Signal>,
                         dispatchers: Vec<Arc<ConnectionDispatcher>>,
                         default_conf_path: String,
                         servers_conf_path: String,
                         token_count: u16,
                         mut srd_http_service: SrdHttpService) {
    // Launch our signal handler thread that will send updated configurations to all our workers
    let (thread_status, _thread_controller) = thread_control::make_pair();
    thread::spawn(move || {
        while thread_status.alive() {
            let signal = signal_channel.recv().unwrap();
            match signal {
                chan_signal::Signal::TERM |
                chan_signal::Signal::INT => {
                    debug!("Received {:?} signal", signal);
                    for dispatcher in &dispatchers {
                        dispatcher.stop();
                    }
                },
                chan_signal::Signal::HUP => {
                    info!("Reloading configuration");
                    let server_conf = SrdServerConf::read_conf_files(default_conf_path.clone(),
                                                                     servers_conf_path.clone(),
                                                                     token_count);
                    srd_http_service.set_conf(server_conf);
                    for dispatcher in &dispatchers {
                        dispatcher.worker_pool().send_service(srd_http_service.clone());
                    }
                },
                _ =>
                    error!("Received unknown signal: {:?}", signal)
            }
        }
    });
}
