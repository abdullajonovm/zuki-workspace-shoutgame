variable "instance_count" {
    type = "string"
    description = "Number of EC2 instances to create in this cluster"
}

variable "security_group" {
    type = "string"
    description = "Security group to assign instances of the cluster"
}

variable "instance_name_prefix" {
    type = "string"
    description = "The instance name's prefix; e.g. 'Nomad Cluster 9-'"
}

variable "instance_name_purpose" {
    type = "string"
    description = "The instance name's purpose; e.g. 'Dev', and 'Demo', and 'Prod'"
}

variable "instance_dns_prefix" {
    type = "string"
    description = "The instance DNS hostname prefix; e.g. 'nc9-'"
}

variable "instance_dns_domain" {
    type = "string"
    description = "The instance DNS domain; e.g. 'shoutgameplay.com'"
}

variable "instance_dns_zone_id" {
    type = "string"
    description = "The instance AWS DNS Zone ID"
}

variable "instance_key_name" {
    type = "string"
    description = "The name of the EC2 Key Pair to use to log in to the instance"
    default = "meinc-ec2-1"
}

variable "registry_pass" {
    type = "string"
    description = "The password to the private Docker Registry. Enter 'x' or 'skip' to skip."
}

variable "nomad_mhz" {
    type = "string"
    default = "100000"
    description = "If specified, override CPU detection with the given value in MHz"
}

variable "consul_bootstrap_count" {
    type = "string"
    description = "The number of Consul servers expected during bootstrapping"
}

variable "consul_ec2_tag_key" {
    type = "string"
    description = "The Consul Auto-Join EC2 Tag Key"
    default = "Nomad Cluster"
}

variable "consul_ec2_tag_value" {
    type = "string"
    description = "The Consul Auto-Join EC2 Tag Value"
}

data "aws_ami" "ubuntu_xenial" {
    owners = ["099720109477"]  # Canonical
    filter {
        name = "name"
        values = ["ubuntu/images/hvm-ssd/ubuntu-xenial-16.04-amd64-server-*"]
    }
    filter {
        name = "virtualization-type"
        values = ["hvm"]
    }
    filter {
        name = "architecture"
        values = ["x86_64"]
    }
    most_recent = true
}

resource "aws_instance" "homogeneous" {
    count = "${var.instance_count}"

    ami = "${data.aws_ami.ubuntu_xenial.id}"
    instance_type = "t2.xlarge"
    key_name = "${var.instance_key_name}"

    tags {
        "Name" = "${var.instance_name_prefix}${count.index+1} (${var.instance_name_purpose})"
        "Resource Group" = "Terraform"
        "Nomad Cluster" = "${var.consul_ec2_tag_value}"
    }

    security_groups = ["${var.security_group}"]

    iam_instance_profile = "nomad_cluster"

    provisioner "local-exec" {
        working_dir = "../../../vagrant-bootstrap"
        command = <<EOF
            fab ec2_install_all \
                --set 'fqdn=${var.instance_dns_prefix}${count.index+1}.${var.instance_dns_domain},registry_pass=${var.registry_pass},nomad_mhz=${var.nomad_mhz},consul_bootstrap_count=${var.consul_bootstrap_count},consul_ec2_tag_key=${var.consul_ec2_tag_key},consul_ec2_tag_value=${var.consul_ec2_tag_value}' \
                -H '${self.public_ip}' \
                -n 5
EOF
    }
}

resource "aws_route53_zone" "main" {
    #zone_id = "${var.instance_dns_zone_id}"
    name = "${var.instance_dns_domain}."
    comment = "Managed by User & Terraform"
    #tags {
    #    ManagedBy = "User"
    #}
    force_destroy = false
    lifecycle {
        prevent_destroy = true
    }
}

resource "aws_route53_record" "main" {
    count   = "${var.instance_count}"

    zone_id = "${aws_route53_zone.main.zone_id}"
    name    = "${var.instance_dns_prefix}${count.index+1}.${var.instance_dns_domain}"
    type    = "CNAME"
    ttl     = "60"

    records = [
        "${element(aws_instance.homogeneous.*.public_dns, count.index)}"
    ]
}
