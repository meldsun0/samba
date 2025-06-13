# Samba

[![GitHub License](https://img.shields.io/badge/license-Apache%202.0-blue?style=flat-square)]()
[![Discord](https://img.shields.io/badge/Chat-on%20Discord-%235865F2?logo=discord&logoColor=white)](https://discord.com/channels/890617081744220180/1301231225276465152)


Samba is an [Ethereum portal client](https://github.com/ethereum/portal-network-specs) written in Java language based
on [Teku](https://github.com/Consensys/teku) and [Besu](https://github.com/hyperledger/besu).
The name is inspired by the shortened name of of rescue dog named Sambayon that is the Spanish translation of an Italian dessert.

> **Note:** Samba is still **under heavy development** and is not yet ready for production use.

## Build Instructions

### Install Prerequisites

* Java 21+

Building on a more recent version of Java is supported, but the resulting build will not work on earlier versions of Java.


### Build and Dist

To create a ready to run distribution:

```shell script
git clone https://github.com/meldsun0/samba
cd samba && ./gradlew

```

This produces:
- Fully packaged distribution in `build/distributions`
### Build and Test

To build, clone this repo and run with `gradle`:

```shell script
git clone https://github.com/meldsun0/samba
cd samba && ./gradlew

```
Add environment variables:

```shell script
export GITHUB_ACTOR=
export GITHUB_TOKEN=

```

After a successful build, distribution packages are available in `build/distributions`.

### Other Useful Gradle Targets

| Target             | Builds                                                  |
|--------------------|---------------------------------------------------------|
| distTar            | Full distribution in build/distributions (as `.tar.gz`) |
| distZip            | Full distribution in build/distributions (as `.zip`)    |
| distDocker         | The `meldsun/samba` docker image                        |
| fatJarAllPlatforms | It creates fatJars for multiple platforms               |
| dockerUpload       | Push imges to Docker Hub                                |
| runContainer       | A docker container running                              |

## Code Style

We use Google's Java coding conventions for the project. To reformat code, run:

```shell script
./gradlew spotlessApply
```

Code style is checked automatically during a build.

## Testing

All the unit tests are run as part of the build, but can be explicitly triggered with:

```shell script
./gradlew test
```

## Docker

To run Samba inside docker you should:

```shell script
./gradlew distDocker 
```
```shell script
docker -p 8545:8545 -p 5051:5051 -p 8008:8008 -p 9000:9000/udp {imageName} --p2p-advertised-ip==$(curl -s ifconfig.me)
```
## Running Hive locally

To run Hive locally against Samba you should follow these instructions: 

Clone Hive:
```shell script
git clone https://github.com/ethereum/hive
cd hive
go build .
go build ./cmd/hiveview  
```

Build a local Docker image from Samba:
```shell script
./gradlew build         
./gradlew distDocker  
```

* Copy ~/samba/hive/samba folder to ~/hive/clients
* Change ~/hive/clients/samba/Dockerfile. Replace the values for ARG `baseimage` and ARG `tag` with the values for the recently created image. 

Run Hive tests:
```shell script
./hive -sim portal -client samba,trin -sim.limit history 
```

```shell script
./hive  --sim devp2p --sim.limit discv5 --client samba --docker.output 
```

If you get the following error on macOS: `can't get docker version: Get "http://unix.sock/v1.25/version": dial unix /var/run/docker.sock: connect: no such file or directory`, you 
need to enable `Allow the default Docker socket to be used (requires password)` in `Settings` -> `Advanced`

View logs output and results:
```shell script
./hiveview --serve --logdir ./workspace/logs
```
## [JSON-RPC API (23)](https://samba-portal-node.postman.co/workspace/Samba-Portal-Node-Workspace~8bf54719-5e6d-4476-8b33-6434dc57d833/request/33150235-eb63c4bf-82ff-477e-a17d-616657e9cdbc?action=share&creator=33150235&ctx=documentation&active-environment=33150235-5c222146-bd60-431b-bb15-f3f9dc8fc9cc)

#### History
- portal_historyAddEnr
- portal_historyDeleteEnr
- portal_historyFindContent
- portal_historyFindNodes
- portal_historyGetContent
- portal_historyGetEnr
- portal_historyLocalContent
- portal_historyLookupEnr
- portal_historyOffer
- portal_historyPing
- portal_historyStore
- portal_historyPutContent
- portal_historyRoutingTableInfo

#### Discv5
- discv5_getEnr,
- discv5_nodeInfo, 
- discv5_updateNodeInfo
- discv5_talkReq
- discv5_findNode
- discv5_routingTableInfo
- discv5_addEnr
- discv5_deleteEnr

When running against Hive:
```shell script
./hive -sim portal -client samba,trin -sim.limit history
```
You should be getting: 

![Tests](https://github.com/user-attachments/assets/9c812ad3-cd17-4abc-9f29-70991a80a71a)

## CLI options

| Command Line Argument        | Default Value   |
|------------------------------|-----------------|
| `--unsafe-private-key=`      |                 |
| `--portal-subnetworks=`      | history-network |
| `--use-default-bootnodes=`   | true            |
| `--p2p-ip=`                  | 0.0.0.0         |
| `--jsonrpc-port=`            | 8545            |
| `--jsonrpc-host=`            | 127.0.0.1       |
| `--disable-json-rpc-server=` | false           |
| `-disable-rest--server=`     | false           |
| `--p2p-advertised-ip=`       | 0.0.0.0         |
| `--logging=`                 | INFO            |
| `--data-path=`               | ./build/samba   |




## Hardware Requirements

Minimum:

Recommended:

TO-DO

## Setup

- Besu or Samba:
  - Mount volume:
    - sudo mkfs.xfs /dev/nvme2n1
    - sudo mkdir -p /mnt/ebs1
    - sudo mount /dev/nvme2n1 /mnt/ebs1
  - Provide correct access:
    - sudo chown -R 1000:1000 /mnt/ebs1
  - Relocating Docker's Data Directory to a Custom Path (e.g., EBS Volume):
    - sudo mkdir -p /etc/docker 
    - sudo nano /etc/docker/daemon.json 
    {
      "data-root": "/mnt/ebs1/docker"
      } 
    - sudo mv /var/lib/docker /mnt/ebs1/docker 
    - sudo systemctl daemon-reexec 
    - sudo systemctl restart docker 
    - docker info | grep "Docker Root Dir"
  - If you are running samba standalone:
    - mkdir /mnt/ebs1/samba
    -  docker run -p 8545:8545 -p 5051:5051 -p 8008:8008 -p 9000:9000/udp  -v /mnt/ebs1/samba:/opt/samba meldsun/instances:samba-standalone-arm64 --p2p-advertised-ip=$(curl -s ifconfig.me)
  - Run Besu:
    - docker run -e HOST_IP=$(curl -s ifconfig.me) -d --name besu --user 1000 -p 8545:8545 -p 9545:9545 -p 9000:9000/udp -v /mnt/ebs1:/data  meldsun/instances:besu-with-samba-arm64 
### Running instances

If you want to run Samba using docker compose with Prometheus and Grafana:

```shell script
HOST_IP=$(curl -s ifconfig.me) SAMBA_DATA_PATH={dataPath}  SAMBA_LOG_PATH={logPath} docker compose up -d
```

| Service        | URL                                            | Notes                              |
| -------------- | ---------------------------------------------- | ---------------------------------- |
| **Prometheus** | [http://localhost:9091](http://localhost:9091) | Confirm `samba:8008/metrcis` is UP |
| **Grafana**    | [http://localhost:3000](http://localhost:3000) | Login: `admin` / `admin`           |

If you want to run it using just the docker image:

```shell script
docker run -p 8545:8545 -p 5051:5051 -p 8008:8008 -p 9000:9000/udp  -v /mnt/ebs1/samba:/data -d  meldsun/instances:samba-standalone-arm64 --p2p-advertised-ip=$(curl -s ifconfig.me)
```



### Useful links
* [Devcon SEA History Expiry and Portal Network session](https://notes.ethereum.org/_XVO7jmXTGOwZmhR5-3T9Q)
* [EIP-4444 Implementation Plan: History Expiry in Ethereum](https://hackmd.io/Dobc38YVQ1qmbbyI6LcFqA)
* [Playground](https://playground.open-rpc.org/?schemaUrl=https://raw.githubusercontent.com/ethereum/portal-network-specs/assembled-spec/jsonrpc/openrpc.json)
* https://eips.ethereum.org/EIPS/eip-7639
* https://eips.ethereum.org/EIPS/eip-4444

### Donations
Our work is entirely funded through grants.
Your contribution will help us to:

* Complete Samba’s full compliance with the Portal History subnetwork.
* Deploy Samba nodes globally, expanding the Portal network.
* Drive the ongoing development of additional subnetworks.


Here is our Gitcoin Project: [Samba](https://explorer.gitcoin.co/#/projects/0x686ab86d2f92275ae09e2034c56c81b3373a058d868c64c837f8df1540baa001)
