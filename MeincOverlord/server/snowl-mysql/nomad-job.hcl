job "snowl-mysql" {
    datacenters = ["dc1"]

    type = "service"

    group "main" {
        count = 1
        task "snowl-mysql-mysql" {
            driver = "docker"
            config {
                image = "scm.shoutgameplay.com:5000/snowl-mysql:1.0"
                port_map {
                    mysql = 3306
                }
                volumes = [
                    "/mnt/host/snowl-mysql:/mnt/host"
                ]
                auth {
                    username = "meinc"
                    password = "%m6wgdKu6lTXdUe9C#BJ8m4WeD^m6!SC"
                    server_address = "scm.shoutgameplay.com:5000"
                }
            }
            resources {
                cpu = 2000
                memory = 500
                network {
                    port "mysql" {}
                }
            }
            logs {
                max_files = 20
                max_file_size = 25
            }
            service {
                name = "${JOB}"
                tags = ["master", "mysql"]
                port = "mysql"
                check {
                    type     = "script"
                    name     = "check_status"
                    command  = "/usr/bin/mysqladmin"
                    args     = ["status"]
                    interval = "30s"
                    timeout  = "5s"
                }
                check {
                    type     = "script"
                    name     = "check_data"
                    command  = "/usr/bin/mysql"
                    args     = ["-sse", "select * from gameplay.app;"]
                    interval = "30s"
                    timeout  = "5s"
                }
                check {
                    type     = "tcp"
                    name     = "check_connect"
                    port     = "mysql"
                    interval = "10s"
                    timeout  = "3s"
                }
                #check_restart {
                #    limit = 2
                #    grace = "60s"
                #    ignore_warnings = false
                #}
            }
        }
        ephemeral_disk {
            migrate = false
            size = 1000
            sticky = true
        }
    }
}
