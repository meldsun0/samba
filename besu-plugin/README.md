# Besu Samba Plugin 

This repository hosts the implementation of the besu-samba plugin.

### Quickstart:
- compile samba-plugin `./gradlew installDist`
- run  `./gradlew runBesu `

### Besu and Samba
If you want to run Besu from source code and run it locally with the plugin you should:
- Clone the Hyperledger Besu repository from GitHub:
```shell script
git clone https://github.com/hyperledger/besu.git
cd besu
```
- Build the project using Gradle:
```shell script
./gradlew build
```
- Create the Plugins Directory:
If not already present, create the plugins directory one level below the besu executable:
```shell script
mkdir -p build/install/besu/plugins
```
- Copy Samba Plugin .jar:
```shell script
core/build/libs/*.jar build/install/besu/plugins/
```
- Run Besu:
```shell script
 ./bin/besu --rpc-http-enabled --rpc-http-api=ADMIN,ETH,SAMBA --plugin-samba-host=0.0.0.0 --rpc-http-host=0.0.0.0  --plugin-samba-host=$(curl -s ifconfig.me)
```
## Dev mode
Generate org.meldsunlabs:samba:1.0-SNAPSHOT library:

```shell script
 ./gradlew publishToMavenLocal
```
Remove the library before if necessary: 
```shell script
  rm -rf ~/.m2/repository/org/meldsunlabs/samba/1.0-SNAPSHOT/
```
Build docker image:
```shell script
  docker build -t samba -f docker/Dockerfile .
```
Run Besu with Samba as plugin:
```shell script
  docker run -e HOST_IP=$(curl -s ifconfig.me)  -d --name besu-samba -p 8545:8545  -p 9545:9545   -p 9000:9000/udp samba
```

#### CLI options | TBD

| Command Line Argument | Default Value |
|-----------------------|---------------|
| `-plugin-samba-host=` | 0.0.0.0       |


## RPC methods

### Get Version
#### `samba_getVersion`

This endpoint is hardcoded value used for testing the plugin. 

```shell
  curl --location --request POST 'http://localhost:8545' --data-raw '{
    "jsonrpc": "2.0",
    "method": "samba_getVersion",
    "params": [],
    "id": 1
  }'
```

#### Result
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": "1.0"
}
```
