use std::collections::HashMap;
use std::fs::File;
use std::io::{BufRead, BufReader};
use std::str::FromStr;

use token::TokenConf;

use serde_json;


#[derive(Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ServerDef {
    #[serde(skip_serializing)]
    server_name:    String,
    #[serde(skip_serializing)]
    server_type:    String,
    base_url:       String,
    #[serde(skip_serializing_if = "String::is_empty")]
    base_url_alias: String
}

impl ServerDef {
    pub fn new() -> ServerDef {
        ServerDef {
            server_name:    Default::default(),
            server_type:    Default::default(),
            base_url:       Default::default(),
            base_url_alias: Default::default(),
        }
    }

    #[allow(dead_code)]
    pub fn with_server_name(mut self, server_name: String) -> ServerDef {
        self.server_name = server_name; self
    }

    pub fn with_server_type(mut self, server_type: String) -> ServerDef {
        self.server_type = server_type; self
    }

    #[allow(dead_code)]
    pub fn with_base_url(mut self, base_url: String) -> ServerDef {
        self.base_url = base_url; self
    }

    #[allow(dead_code)]
    pub fn with_base_url_alias(mut self, base_url_alias: String) -> ServerDef {
        self.base_url_alias = base_url_alias; self
    }
}


#[derive(Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct SrdConf {
    doc_type:    &'static str,
    ttl_seconds: u32,
    // tbd -> tbd
    data:        HashMap<String, String>,
    // action_name -> server_type
    actions:     HashMap<String, String>,
    // server_type -> server_defs
    servers:     HashMap<String, Vec<ServerDef>>,
}

impl SrdConf {
    fn new() -> SrdConf {
        SrdConf {
            doc_type:    "srd/3.0",
            ttl_seconds: 300,
            data:        Default::default(),
            actions:     Default::default(),
            servers:     Default::default(),
        }
    }

    fn push_server(&mut self, server: ServerDef) {
        let server_type = server.server_type.clone();
        if ! self.servers.contains_key(&server.server_type) {
            let server_vec = Vec::new();
            self.servers.insert(server_type.clone(), server_vec);
        }
        self.servers.get_mut(&server_type).unwrap().push(server);
    }

    fn push_action<S: ToString>(&mut self, action_name: S, server_type: S) {
        let action_name = action_name.to_string();
        let server_type = server_type.to_string();
        if self.actions.contains_key(&action_name) {
            panic!("Duplicate action: {} -> {}", action_name, server_type);
        }
        self.actions.insert(action_name, server_type);
    }

    fn read_conf_file(path: String) -> SrdConf {
        let srd_conf_file = File::open(path).expect("SRD conf file not found");
        let srd_conf_reader = BufReader::new(srd_conf_file);

        let mut srd_conf = SrdConf::new();

        #[derive(Clone)]
        enum ReadConfState {
            None,
            Server(ServerDef),
        }

        let mut read_state = ReadConfState::None;
        let mut next_state = None;
        let mut line_num = 0u32;
        for srd_conf_line_result in srd_conf_reader.lines() {
            let mut srd_conf_line = srd_conf_line_result.unwrap();
            line_num += 1;

            if srd_conf_line.trim().is_empty() || srd_conf_line.trim_left().starts_with("#") {
                continue;
            }

            loop {
                if let Some(state) = next_state.take() {
                    read_state = state;
                }
                match read_state {
                    ReadConfState::None => {
                        if srd_conf_line.starts_with("ttlSeconds: ") {
                            srd_conf_line.drain(.."ttlSeconds: ".len());
                            srd_conf.ttl_seconds = match u32::from_str(&*srd_conf_line) {
                                Ok(ttl) => ttl,
                                Err(e) => panic!("Invalid ttl seconds value on line {}: {}", line_num, e)
                            };
                        } else if srd_conf_line.starts_with("action: ") {
                            srd_conf_line.drain(.."action: ".len());
                            let srd_conf_line_parts: Vec<_> = srd_conf_line.split("->").collect();
                            if srd_conf_line_parts.len() != 2 {
                                panic!("Illegal action on line {}", line_num);
                            }
                            srd_conf.push_action(srd_conf_line_parts[0].trim(), srd_conf_line_parts[1].trim());
                        } else if srd_conf_line.starts_with("server: ") {
                            srd_conf_line.drain(.."server: ".len());
                            debug!("Found server {}", srd_conf_line);
                            let mut server_def = ServerDef::new();
                            server_def.server_name = srd_conf_line.clone();
                            next_state = Some(ReadConfState::Server(server_def));
                        } else {
                            panic!("Illegal configuration found on line {}", line_num);
                        }
                    },
                    ReadConfState::Server(ref mut server_def) => {
                        if ! srd_conf_line.starts_with("\t") {
                            if server_def.server_name.is_empty()
                                    || server_def.server_type.is_empty()
                                    || server_def.base_url.is_empty() {
                                panic!("Incomplete server definition ending on line {}", line_num);
                            }
                            srd_conf.push_server(server_def.clone());
                            next_state = Some(ReadConfState::None);
                            continue;
                        } else if srd_conf_line.starts_with("\ttype: ") {
                            srd_conf_line.drain(.."\ttype: ".len());
                            server_def.server_type = srd_conf_line;
                        } else if srd_conf_line.starts_with("\tbase_url: ") {
                            srd_conf_line.drain(.."\tbase_url: ".len());
                            server_def.base_url = srd_conf_line;
                        } else if srd_conf_line.starts_with("\tbase_url_alias: ") {
                            srd_conf_line.drain(.."\tbase_url_alias: ".len());
                            server_def.base_url_alias = srd_conf_line;
                        } else {
                            panic!("Illegal configuration found on line {}", line_num);
                        }
                    },
                }
                break;
            }
        }
        if let ReadConfState::Server(server_def) = read_state {
            srd_conf.push_server(server_def);
        }
        srd_conf
    }
}


#[derive(Clone)]
pub struct ServersConf {
    // server_type -> server_defs
    pub servers: HashMap<String, Vec<ServerDef>>,
}

impl ServersConf {
    fn new() -> ServersConf {
        ServersConf {
            servers: Default::default(),
        }
    }

    fn push_server(&mut self, server_def: ServerDef) {
        let server_defs;
        {
            let server_type = &server_def.server_type;
            if ! self.servers.contains_key(server_type) {
                let servers_vec = Vec::new();
                self.servers.insert(server_type.clone(), servers_vec);
            }
            server_defs = self.servers.get_mut(&*server_type).unwrap();
        }
        server_defs.push(server_def);
    }

    fn read_conf_file(path: String) -> ServersConf {
        let servers_conf_file = File::open(path).expect("Servers conf file not found");
        let servers_conf_reader = BufReader::new(servers_conf_file);

        let mut servers_conf = ServersConf::new();

        #[derive(Clone)]
        enum ReadConfState {
            None,
            ServerType(ServerDef),
            ServerName(ServerDef),
        }

        let mut read_state = ReadConfState::None;
        let mut next_state = None;
        let mut line_num = 0u32;
        for servers_conf_line_result in servers_conf_reader.lines() {
            let mut servers_conf_line = servers_conf_line_result.unwrap();
            line_num += 1;

            if servers_conf_line.trim().is_empty() || servers_conf_line.trim_left().starts_with("#") {
                continue;
            }

            loop {
                if let Some(state) = next_state.take() {
                    read_state = state;
                }
                match read_state {
                    ReadConfState::None => {
                        if !servers_conf_line.starts_with("\t")
                                && servers_conf_line.trim_right().ends_with(":") {
                            let colon_idx = servers_conf_line.rfind(':').unwrap();
                            servers_conf_line.truncate(colon_idx);
                            let server_type = servers_conf_line.trim().to_string();
                            let mut server_def = ServerDef::new();
                            server_def.server_type = server_type;
                            next_state = Some(ReadConfState::ServerType(server_def.clone()));
                        } else {
                            panic!("Illegal servers configuration found on line {}", line_num);
                        }
                    },
                    ReadConfState::ServerType(ref mut server_def) => {
                        if !servers_conf_line.starts_with("\t") {
                            // Ignore one-line servers
                            next_state = Some(ReadConfState::None);
                            continue;
                        }
                        if servers_conf_line.starts_with("\t\t")
                                || !servers_conf_line.trim_right().ends_with(":") {
                            panic!("Illegal servers configuration found on line {}", line_num);
                        }
                        let colon_idx = servers_conf_line.rfind(':').unwrap();
                        servers_conf_line.truncate(colon_idx);
                        let server_name = servers_conf_line.trim().to_string();
                        server_def.server_name = server_name;
                        next_state = Some(ReadConfState::ServerName(server_def.clone()));
                    },
                    ReadConfState::ServerName(ref mut server_def) => {
                        if !servers_conf_line.starts_with("\t\t") {
                            if server_def.server_name.is_empty()
                                    || server_def.server_type.is_empty()
                                    || server_def.base_url.is_empty() {
                                panic!("Incomplete server definition ending on line {}", line_num);
                            }
                            servers_conf.push_server(server_def.clone());
                            if servers_conf_line.starts_with("\t") {
                                let server_def = ServerDef::new()
                                    .with_server_type(server_def.server_type.clone());
                                next_state = Some(ReadConfState::ServerType(server_def));
                            } else {
                                next_state = Some(ReadConfState::None);
                            }
                            continue;
                        } else {
                            let server_url = servers_conf_line.trim().to_string();
                            if server_def.base_url.is_empty() {
                                server_def.base_url = server_url;
                            } else if server_def.base_url_alias.is_empty() {
                                server_def.base_url_alias = server_url;
                            } else {
                                panic!("Too many server URL's on line {}", line_num);
                            }
                            next_state = Some(ReadConfState::ServerName(server_def.clone()));
                        }
                    },
                }
                break;
            }
        }
        if let ReadConfState::ServerName(server_def) = read_state {
            servers_conf.push_server(server_def);
        }
        servers_conf
    }
}


#[derive(Clone)]
pub struct SrdServerConf {
    pub srd_conf: SrdConf,
    pub servers_conf: ServersConf,
    pub merged_conf: SrdConf,
    // server_type -> token_conf
    pub server_tokens: HashMap<String, TokenConf<ServerDef>>,
}

impl SrdServerConf {
    pub fn read_conf_files(default_path: String, servers_path: String, token_count: u16) -> SrdServerConf {
        let srd_conf = SrdConf::read_conf_file(default_path);

        let mut servers_conf = ServersConf::read_conf_file(servers_path);

        let mut merged_conf = srd_conf.clone();
        for (server_type, server_defs) in &mut servers_conf.servers {
            merged_conf.servers.insert(server_type.clone(), server_defs.clone());
        }

        let mut server_tokens = HashMap::new();
        let mut merged_server_defs = HashMap::new();
        for (server_type, server_defs) in &srd_conf.servers {
            merged_server_defs.insert(server_type.clone(), server_defs.clone());
        }
        for (server_type, server_defs) in &servers_conf.servers {
            if merged_server_defs.contains_key(server_type) {
                merged_server_defs.get_mut(server_type).unwrap().extend(server_defs.clone());
            } else {
                merged_server_defs.insert(server_type.clone(), server_defs.clone());
            }
        }
        for (server_type, mut server_defs) in merged_server_defs {
            server_defs.sort_unstable_by_key(|sd| sd.server_name.clone());
            let token_conf = TokenConf::new(token_count, server_defs);
            server_tokens.insert(server_type.clone(), token_conf);
        }

        SrdServerConf {
            srd_conf: srd_conf,
            servers_conf: servers_conf,
            merged_conf: merged_conf,
            server_tokens: server_tokens,
        }
    }

    pub fn client_json(&self, client_hash_val: u64) -> serde_json::Result<String> {
        let mut client_srd = self.merged_conf.clone();
        let mut client_servers = HashMap::new();
        for (server_type, server_defs) in &client_srd.servers {
            if server_defs.len() == 1 {
                client_servers.insert(server_type.clone(), server_defs.clone());
            } else if server_defs.len() > 1 {
                match self.server_tokens.get(server_type) {
                    Some(token_conf) => {
                        let client_server_def = token_conf.domain(client_hash_val);
                        client_servers.insert(server_type.clone(), vec!(client_server_def.clone()));
                    },
                    None => {
                        client_servers.insert(server_type.clone(), server_defs.clone());
                    }
                }
            }
        }
        client_srd.servers = client_servers;
        serde_json::to_string(&client_srd)
    }

    #[allow(dead_code)]
    pub fn default_json(&self) -> serde_json::Result<String> {
        serde_json::to_string(&self.srd_conf)
    }
}
