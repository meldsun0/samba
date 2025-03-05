# Besu Samba Plugin 

This repository hosts the implementation of the besu-samba plugin.

### Quickstart:
- compile samba-plugin `gradlew installDist`
- run  `./gradlew runBesu `



#### CLI options | TBD

| Command Line Argument | Default Value |
|-----------------------|---------------|
| `--samba-xyz`         | xyz           |


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
