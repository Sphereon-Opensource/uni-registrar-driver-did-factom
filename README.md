![Factom Protocol Logo](https://assets.website-files.com/5bca6108bae718b9ad49a5f9/5c4820477febe49184787777_Factom-Protocol_Logo-p-500.png)

# Universal Registrar Driver: Factom

This is a Java [Universal Registrar](https://github.com/decentralized-identity/universal-registrar/) driver for **did:factom** identifiers 

## Specifications

* [Decentralized Identifiers](https://w3c.github.io/did-core/)
* [Factom Decentralized Identifiers](https://github.com/factom-protocol/FIS/blob/master/FIS/DID.md), see note below!

## Build and Run (Docker)

```
docker build -f ./Dockerfile . -t sphereon/uni-registrar-driver-did-factom
docker run -p 9080:9080 sphereon/uni-registrar-driver-did-factom
curl -X POST http://localhost:9080/1.0/create -H "Content-Type: application/json"
```
## Required option parameters:
An example call with the option parameters can be seen below:
```shell script
curl -X POST http://localhost:9080/1.0/create -H "Content-Type: application/json" -d \
'{
 "options": {
    "didVersion": "FACTOM_V1_JSON",
    "managementKeys": [
      {
        "type": "Ed25519VerificationKey2020",
        "keyIdentifier": "management-0",
        "publicKeyMultibase": "zAAC2AVvLMv6gmMNam3uVAjZpfkcJCwDwnZn6z3wXmqPV",
        "priority": 0
      }
    ],
    "didKeys": [
      {
        "type": "Ed25519VerificationKey2018",
        "keyIdentifier": "public-0",
        "publicKeyBase58": "AAVAjZpfMv6gmMNam3uVAjZpfkcJCwDwnZn6MNam3uVA",
        "priorityRequirement": 1,
        "purpose": [
          "publicKey"
        ]
      }
    ],
    "tags": [
      "my-app",
      "more-info",
      "id-example"
    ],
    "nonce": "a8256aef-cf53-41a3-9343-25c0765e5187",
    "networkName": "mainnet"
  }
}'
```
The options are:
* didVersion - REQUIRED: will be used for backwards compatibility with Factom Identity DIDs (the previous version), but currently only ”FACTOM_V1_JSON” is supported.

* managementKeys - REQUIRED: an array of management keys that will be used to update the DID document (see Key Objects below)

* didKeys - REQUIRED: an array of did keys that will appear in the resulting DID document. These are the keys that will be used by the identity to sign VCs or presentations (see Key Objects below).

* tags - REQUIRED: an array of strings that will be the external ids for the chain entry forming the basis of the DID. These do not have to be unique as the nonce will determine the unique DID URI.

* nonce - REQUIRED: a unique string used to determine the unique DID URI.

* networkName - OPTIONAL: should be either "testnet" or "mainnet" and determines where the DID will be created. 

## Build and Run (Maven)

```
mvn clean install
```

## Driver Environment Variables

The driver recognizes the following environment variables:

### `uniregistrar_driver_did_factom_exampleSetting`

* An example setting for the driver.
* DidController value: (empty string)

## Driver Metadata

The driver returns the following metadata in addition to a DID document:

* `exampleMetadata`: Example metadata
