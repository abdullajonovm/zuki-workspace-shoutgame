job "snowl-nomad-web-proxy" {
    datacenters = ["dc1"]

    type = "system"

    group "main" {
        #count = 1
        task "snowl-nomad-web-proxy-nginx" {
            driver = "docker"
            config {
                hostname = "${NOMAD_JOB_NAME}--${attr.unique.hostname}"
                image = "scm.shoutgameplay.com:5000/snowl-nomad-web-proxy:dev"
                port_map {
                    http = 80
                    https = 443
                }
                volumes = [
                    "/mnt/host/snowl-nomad-web-proxy:/mnt/host",
                ]
                auth {
                    username = "meinc"
                    password = "%m6wgdKu6lTXdUe9C#BJ8m4WeD^m6!SC"
                    server_address = "scm.snowl.com:5000"
                }
            }
            env {
                "OVERWRITE_VOLUME_FILES" = "false"
                "PROXY_ALL_CLUSTER_SERVERS" = "true"
            }
            resources {
                cpu = 1000
                memory = 50
                network {
                    port "http" { static = 80 }
                    port "https" { static = 443 }
                }
            }
            logs {
                max_files = 20
                max_file_size = 25
            }
            #service {
            #    name = "${JOB}"
            #    tags = ["http"]
            #    port = "http"
            #}
            #service {
            #    name = "${JOB}"
            #    tags = ["https"]
            #    port = "https"
            #}
        }
        ephemeral_disk {
            migrate = false
            size = 1000
            sticky = false
        }
    }
}
