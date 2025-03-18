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

| Target       | Builds                                                  |
|--------------|---------------------------------------------------------|
| distTar      | Full distribution in build/distributions (as `.tar.gz`) |
| distZip      | Full distribution in build/distributions (as `.zip`)    |
| distDocker   | The `meldsun/samba` docker image                        |
| dockerUpload | Push imges to Docker Hub                                |
| runContainer | A docker container running                              |

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

## Running Hive locally

To run Hive locally against Samab you should follow these instractions: 

Clone Hive:
```shell script
git clone https://github.com/ethereum/hive
go build .
go build ./cmd/hiveview  
```

Build a local Docker image from Samba:
```shell script
./gradlew build         
./gradlew distDocker  
```

* Copy hive/samba folder to hive/clients
* Change /hive/samba/Dockerfile by adding the recent created image. 

Run Hive tests:
```shell script
./hive -sim portal -client samba,trin -sim.limit history 
```
View logs output and results:
```shell script
./hiveview --serve --logdir ./workspace/logs
```
## JSON-RPC API (14)
- portal_historyAddEnr
- portal_historyDeleteEnr
- portal_historyFindContent
- portal_historyFindNodes
- portal_historyGetContent
- portal_historyGetEnr
- portal_historyLocalContent
- potal_historyLookupEnr
- portal_historyOffer
- portal_historyPing
- portal_historyStore

- discv5_getEnr,
- discv5_nodeInfo, 
- discv5_updateNodeInfo

When running against Hive:
```shell script
./hive -sim portal -client samba,trin -sim.limit history
```
You should be getting: 

![Tests](https://github.com/user-attachments/assets/9c812ad3-cd17-4abc-9f29-70991a80a71a)

## Hardware Requirements

Minimum:

TO-DO

Recommended:

TO-DO


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
