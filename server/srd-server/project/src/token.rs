use std::collections::HashMap;


#[derive(Clone)]
struct Token(u16);


#[derive(Clone)]
pub struct TokenConf<DomainT> {
    tokens: Vec<Token>,
    domains: Vec<DomainT>,
    domain_index_by_token_index: HashMap<usize, usize>,
}

impl<DomainT> TokenConf<DomainT> {
    pub fn new(token_count: u16, domains: Vec<DomainT>) -> TokenConf<DomainT> {
        if domains.len() > token_count as usize {
            panic!("Domains outnumber tokens: {} vs {}", domains.len(), token_count);
        }

        let mut tokens = Vec::with_capacity(token_count as usize);
        for i in 0..token_count {
            tokens.push(Token(i));
        }

        let mut domain_index_by_token_index = HashMap::with_capacity(token_count as usize);
        {
            let mut domains_iter = domains.iter().enumerate().cycle();
            for (token_index, _token) in tokens.iter().enumerate() {
                match domains_iter.next() {
                    Some((domain_index, _domain)) => {
                        domain_index_by_token_index.insert(token_index, domain_index);
                    },
                    None =>
                        panic!("Illegal state error")
                }
            }
        }
        TokenConf {
            tokens: tokens,
            domains: domains,
            domain_index_by_token_index: domain_index_by_token_index,
        }
    }

    #[allow(dead_code)]
    fn token(&self, token_hash: u64) -> &Token {
        let token_index = (token_hash % self.tokens.len() as u64) as usize;
        &self.tokens[token_index]
    }

    pub fn domain(&self, token_hash: u64) -> &DomainT {
        let token_index = (token_hash % self.tokens.len() as u64) as usize;
        let domain_index = self.domain_index_by_token_index[&token_index];
        &self.domains[domain_index]
    }
}
