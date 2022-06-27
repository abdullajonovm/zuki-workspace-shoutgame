variable "registry_pass" {
    type = "string"
    description = "The password to the private Docker Registry. Enter 'x' to skip this step."
}

provider "aws" {
    region = "us-east-1"
    profile = "terraform"
}

module "homogeneous" {
    source  = "../../modules/homogeneous"

    instance_count = 2
    consul_bootstrap_count = 1
    security_group = "nomad-cluster-9"
    instance_name_prefix = "Nomad Cluster 9-"
    instance_name_purpose = "Dev"
    instance_dns_prefix = "nc9-"
    instance_dns_domain = "shoutgameplay.com"
    instance_dns_zone_id = "Z4WL1XIAZQK94"
    consul_ec2_tag_key = "Nomad Cluster"
    consul_ec2_tag_value = "9"
    registry_pass = "${var.registry_pass}"
}
