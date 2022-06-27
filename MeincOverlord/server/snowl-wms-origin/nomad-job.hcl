job "snowl-wms-origin" {
    datacenters = ["dc1"]

    type = "service"

    group "main" {
        count = 1
        task "snowl-wms-origin-nginx" {
            driver = "docker"
            config {
                image = "scm.shoutgameplay.com:5000/snowl-wms-origin:dev"
                port_map {
                    http = 80
                    control = 81
                    https = 443
                }
                volumes = [
                    "/mnt/host/snowl-wms-origin:/mnt/host",
                ]
                #volume_driver = "local"
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
                memory = 500
                network {
                    port "http" {}
                    port "control" {}
                    port "https" {}
                }
            }
            logs {
                max_files = 20
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
            service {
                name = "${JOB}"
                tags = ["control"]
                port = "control"
            }
        }
        ephemeral_disk {
            migrate = false
            size = 1000
            sticky = true
        }
    }
}
