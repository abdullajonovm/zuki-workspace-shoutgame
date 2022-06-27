job "snowl-srd-server" {
    datacenters = ["dc1"]

    type = "service"

    group "main" {
        count = 1
        constraint {
            distinct_hosts = true
        }
        task "snowl-srd-server" {
            driver = "docker"
            config {
                image = "scm.shoutgameplay.com:5000/snowl-srd-server:dev"
                port_map {
                    http = 8080
                    https = 8443
                }
                volumes = [
                    "/mnt/host/snowl-srd-server:/mnt/host",
                ]
                auth {
                    username = "meinc"
                    password = "%m6wgdKu6lTXdUe9C#BJ8m4WeD^m6!SC"
                    server_address = "scm.shoutgameplay.com:5000"
                }
            }
            env {
                "OVERWRITE_VOLUME_FILES" = "false"
            }
            resources {
                cpu = 2000
                memory = 50
                network {
                    port "http" {}
                    port "https" {}
                }
            }
            logs {
                max_files = 10
                max_file_size = 25
            }
            service {
                name = "${JOB}--${NOMAD_ALLOC_INDEX}"
                tags = ["http"]
                port = "http"
            }
            service {
                name = "${JOB}--${NOMAD_ALLOC_INDEX}"
                tags = ["https"]
                port = "https"
            }
        }
    }
}
