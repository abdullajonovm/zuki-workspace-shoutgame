job "snowl-socketio" {
    datacenters = ["dc1"]

    type = "service"

    group "main" {
        count = 1
        task "snowl-socketio" {
            driver = "docker"
            config {
                image = "scm.shoutgameplay.com:5000/snowl-socketio:dev"
                port_map {
                    http = 8080
                    https = 8443
                }
                volumes = [
                    "/mnt/host/snowl-socketio:/mnt/host",
                ]
                auth {
                    username = "meinc"
                    password = "%m6wgdKu6lTXdUe9C#BJ8m4WeD^m6!SC"
                    server_address = "scm.shoutgameplay.com:5000"
                }
            }
            env {
                #"DEBUG" = "*"
                "OVERWRITE_VOLUME_FILES" = "false"
            }
            resources {
                cpu = 2000
                memory = 200
                network {
                    port "http" {}
                    #TODO enable TLS
                    #port "https" {}
                }
            }
            logs {
                max_files = 20
                max_file_size = 25
            }
            service {
                name = "${JOB}"
                tags = ["http"]
                port = "http"
            }
            service {
                name = "${JOB}--${NOMAD_ALLOC_INDEX}"
                tags = ["http"]
                port = "http"
            }
            #service {
            #    name = "${JOB}--${NOMAD_ALLOC_INDEX}"
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
