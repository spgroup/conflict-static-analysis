# Containerize an application

## Configure the server

1. Make sure docker is installed and updated on the target machine. Run `docker -v`.
2. Copy `Dockerfile` and `settings.xml` to target machine.
3. Update **username** and **password** in servers context in `settings.xml` file.

## Build the app’s container image

Run `docker build -t getting-started .`

This command used the Dockerfile to build a new container image.

> The `-t` flag tags our image. Think of this simply as a human-readable name for the final image. Since we named the image `getting-started`, we can refer to that image when we run a container.

> The `.` at the end of the docker build command tells Docker that it should look for the `Dockerfile` in the current directory.

## Start an app container

Start your container using the docker run command and specify the name of the image we just created:

Run `docker run -dp 3000:3000 getting-started`

> Remember the -d and -p flags? We’re running the new container in “detached” mode (in the background) and creating a mapping between the host’s port 3000 to the container’s port 3000. Without the port mapping, we wouldn’t be able to access the application.

## Accessing Docker Containers

Once your implementation is up and running, you can access your Docker containers to get started using the app

Run the Docker list command to get a list of all Docker containers running on the system:

`docker container ls`

For remote access, run:

`docker exec –it <container name> /bin/bash`

## Closing Docker Containers

To stop and remove a docker container, run the following command:

`docker stop <container name>`
