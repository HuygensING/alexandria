## To run the docker image:

    docker run -d -p{local_main_port}:8080 -p{local_admin_port}:8081 -e BASE_URI=http://{local_server_name}:{local_main_port} huygensing/alexandria-markup-server

Then go to http://{local_server_name}:{local_main_port} to see the site.

## Upload a new [lmnl](http://lmnl-markup.org/specs/archive/LMNL_syntax.xhtml) file:

    curl -i -H 'Content-type:text/plain' --data-binary @example.lmnl http://{local_server_name}:{local_main_port}/documents/lmnl

## Upload a new [TexMECS](http://xml.coverpages.org/MLCD-texmecs20010510.html) file:

    curl -i -H 'Content-type:text/plain' --data-binary @example.lmnl http://{local_server_name}:{local_main_port}/documents/texmecs
        