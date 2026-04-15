# Containerize an application

## Configure the server

1. Make sure docker is installed and updated on the target machine. Run `docker -v`.
2. Copy `Dockerfile` and `settings.xml` to target machine.
3. Update **username** and **password** in servers context in `settings.xml` file.

## Build the app’s container image

Run `docker build --no-cache -t run-experiment .`

This command used the Dockerfile to build a new container image.

> The `-t` flag tags our image. Think of this simply as a human-readable name for the final image. Since we named the image `getting-started`, we can refer to that image when we run a container.

> The `.` at the end of the docker build command tells Docker that it should look for the `Dockerfile` in the current directory.

## Start an app container

Change the entrypoint to an interactive shell
Start your container using the docker run command and specify the name of the image we just created:

Run `docker run --entrypoint /bin/sh -itd run-experiment:latest`

> This is useful if you want to use a container like a virtual machine, and keep it running in the background when you’re not using it. If you override the entrypoint to the container with a shell, like sh or bash, and run the container with the -itd switches, Docker will run the container, detach it (run it in the background), and attach an interactive terminal. Note that this won’t run the container’s default entrypoint script, but run a shell instead. Effectively, you will have a container that stays running permanently.

## Accessing Docker Containers

Once your implementation is up and running, you can access your Docker containers to get started using the app

Run the Docker list command to get a list of all Docker containers running on the system:

`docker container ls`

For remote access, run:

`docker exec -it <container-id> /bin/bash`

## Running the Experiment Multiple Times

To run the experiment ten times (as defined by the default configuration), access the container and execute the script:

```bash 
cd /home && ./entrypoint.sh
```

### Copying Results from the Container

Use the docker cp command to copy result files from the container to your local machine:

```bash
docker cp <container-id>:/home/mds/miningframework/results <your-local-path>
docker cp <container-id>:/home/rds/miningframework/results <your-local-path>
```

## Running Analysis Manually

At the root of the mining framework, if you want to run the analyses outside of the experiment infrastructure, you can
run, for example:

```bash
./gradlew run -DmainClass="services.outputProcessors.soot.Main" --args="--ioa --ioa-without-pa"`
```

You can pass the `-l` flag to set the prodnfdity limit and the `-t` flag to set the timeout.

### To retrieve the output files:

```bash
docker cp <container-id>:/miningframework/out.txt <seu-caminho>
docker cp <container-id>:/miningframework/outConsole.txt <seu-caminho>
docker cp <container-id>:/miningframework/time.txt <seu-caminho>
docker cp <id-do-container>:/miningframework/output/data/soot-results.csv <seu-caminho>
```

## Stopping and Removing a Docker Container

To stop and remove a Docker container, run the following command:

`docker stop <id-do-container>`