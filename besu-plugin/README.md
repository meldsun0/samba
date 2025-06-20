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
- Install distribution:
```shell script
./gradlew installDist 
```
- Create the Plugins Directory:
If not already present, create the plugins directory one level below the besu executable:
```shell script
mkdir -p build/install/besu/plugins
```
- Copy Samba Plugin .jar:
```shell script
cp core/build/libs/*.jar build/install/besu/plugins/
```
- Run Besu:
```shell script
 ./build/install/besu/bin/besu --rpc-http-enabled --rpc-http-api=ADMIN,ETH,SAMBA
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
  docker run -e HOST_IP=$(curl -s ifconfig.me) -e LOG_LEVEL=INFO -d --name besu-samba -p 8545:8545  -p 9545:9545   -p 9000:9000/udp samba
```

#### CLI options | TBD

| Command Line Argument | Default Value |
|-----------------------|---------------|
| `-plugin-samba-host=` | 0.0.0.0       |


## RPC methods
- samba_getBlockBodyByBlockHash
- samba_getBlocHeaderByBlockHash
- samba_transactionReceiptByBlockHash
- samba_getBlockHeaderByBlockNumber

### Get BlockBody by Blockhash:
#### `samba_getBlockBodyByBlockHash`

```shell
curl --location 'xyz:8545' \
--header 'Content-Type: application/json' \
--data '{
"jsonrpc": "2.0",
"method": "samba_getBlockBodyByBlockHash",
"params": ["0xcb278a973370d2b1ba0cd9e0f25d1329ccb11d7ddca21c9df5ba2362d59ff2d3"],
"id": 1
}'
```

## Interface available:
```
@Unstable
public interface HistoryService {

  Optional<BlockHeader> getBlockHeaderByBlockHash(Hash blockHash);

  Optional<BlockBody> getBlockBodyByBlockHash(Hash blockHash);

  Optional<List<TransactionReceipt>> getTransactionReceiptByBlockHash(Hash blockHash);

  //The characters in the string must all be decimal digits
  Optional<BlockHeader> getBlockHeaderByBlockNumber(String blockNumber);
}
```