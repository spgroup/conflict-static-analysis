# Containerize an application

## Configure the server

1. Make sure docker is installed and updated on the target machine. Run `docker -v`.
2. Copy `Dockerfile` and `settings.xml` to target machine.
3. Update **username** and **password** in servers context in `settings.xml` file.
4. update **TAG_BRANCH_OR_COMMIT_REFS** on line 61, 63 and 65 in `Dockerfile`. To specify the version you want the build
   to be performed on.

## Build the app’s container image

Run `docker build -t getting-started .`

This command used the Dockerfile to build a new container image.

> The `-t` flag tags our image. Think of this simply as a human-readable name for the final image. Since we named the image `getting-started`, we can refer to that image when we run a container.

> The `.` at the end of the docker build command tells Docker that it should look for the `Dockerfile` in the current directory.

## Start an app container

Change the entrypoint to an interactive shell Start your container using the docker run command and specify the name of
the image we just created:

Run `docker run --entrypoint /bin/sh -itd getting-started:latest`

> This is useful if you want to use a container like a virtual machine, and keep it running in the background when you’re not using it. If you override the entrypoint to the container with a shell, like sh or bash, and run the container with the -itd switches, Docker will run the container, detach it (run it in the background), and attach an interactive terminal. Note that this won’t run the container’s default entrypoint script, but run a shell instead. Effectively, you will have a container that stays running permanently.

## Accessing Docker Containers

Once your implementation is up and running, you can access your Docker containers to get started using the app

Run the Docker list command to get a list of all Docker containers running on the system:

`docker container ls`

For remote access, run:

`docker exec –it <container name> /bin/bash`

## Closing Docker Containers

To stop and remove a docker container, run the following command:

`docker stop <container name>`
