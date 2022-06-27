job "snowl-collector" {
    datacenters = ["dc1"]

    type = "service"

    group "main" {
        count = 1
        task "snowl-collector-mrsoa" {
            driver = "docker"
            config {
                hostname = "${NOMAD_JOB_NAME}--${NOMAD_ALLOC_INDEX}--${attr.unique.hostname}"
                image = "scm.shoutgameplay.com:5000/snowl-collector:dev"
                port_map {
                    http = 8080
                    https = 8443
                    mrsoa = 9119
                    hazelcast = 5701
                    hazelcast_shared = 6701
                    java_debugger = 8000
                    java_profiler = 10001
                }
                volumes = [
                    "/mnt/host/snowl-collector:/mnt/host",
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
                memory = 2000
                network {
                    port "http" {}
                    port "https" {}
                    port "mrsoa" {}
                    port "hazelcast" {}
                    port "hazelcast_shared" {}
                    port "java_debugger" {}
                    port "java_profiler" {}
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
                tags = ["https", "->srd"]
                port = "https"
            }
            service {
                name = "snowl-vscollector"
                tags = ["https", "->srd"]
                port = "https"
            }
            service {
                name = "${JOB}--${NOMAD_ALLOC_INDEX}"
                tags = ["mrsoa"]
                port = "mrsoa"
            }
            service {
                name = "${JOB}--${NOMAD_ALLOC_INDEX}"
                tags = ["hazelcast"]
                port = "hazelcast"
            }
            service {
                name = "${JOB}--${NOMAD_ALLOC_INDEX}"
                tags = ["java_debugger"]
                port = "java_debugger"
            }
            service {
                name = "${JOB}--${NOMAD_ALLOC_INDEX}"
                tags = ["java_profiler"]
                port = "java_profiler"
            }
            service {
                name = "snowl-mrsoa"
                tags = ["hazelcast-shared"]
                port = "hazelcast_shared"
            }
        }
        ephemeral_disk {
            migrate = false
            size = 1000
            sticky = false
        }
    }
}
