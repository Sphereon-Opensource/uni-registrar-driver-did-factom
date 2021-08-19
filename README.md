<h1 align="center">
  <br>
  <a href="https://www.sphereon.com"><img src="https://sphereon.com/content/themes/sphereon/assets/img/logo.svg" alt="Sphereon" width="400"></a>
  <br>
Universal Registrar Driver: Factom
  <br>
</h1>



# About
This is a Java [Universal Registrar](https://github.com/decentralized-identity/universal-registrar/) driver for **did:factom** identifiers 

## Specifications

* [Decentralized Identifiers](https://w3c.github.io/did-core/)
* [Factom Decentralized Identifiers](https://github.com/factom-protocol/FIS/blob/master/FIS/DID.md), see note below!

## DID creation and wrapped native identities
The Factom Univeral Registrar driver accepts 2 types of requests to created DIDs.

The first and suggested method is to create full fledged DIDs on Factom conforming to the Factom DID specification (see Option 1 below).

The second method is to have DIDs that wrap native Factom Identities (idpub chains). These DIDs are more restricted, as we derive fixed Verification Methods and have no support for Services for instance. It is however simpler to use and wraps existing Factom Native identities (see Option 2 below)


### Option 1: Full Factom DID Spec V1 option parameters:
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
    "networkName": "testnet"
  }
}'
```
The options are:
* didVersion - REQUIRED:  Describes this mode (FACTOM_V1_JSON), or backwards compatibility with Factom Identity chains (FACTOM_IDENTITY_CHAIN), see next chapter.

* managementKeys - REQUIRED: an array of management keys that will be used to update the DID document (see Key Objects below)

* didKeys - REQUIRED: an array of did keys that will appear in the resulting DID document. These are the keys that will be used by the identity to sign VCs or presentations (see Key Objects below).

* tags - OPTIONAL: an array of strings that will be the external ids for the chain entry forming the basis of the DID. These do not have to be unique as the nonce will determine the unique DID URI.

* nonce - REQUIRED: a unique string used to determine the unique DID URI.

* networkName - OPTIONAL: should be either "testnet" or "mainnet" and determines where the DID will be created. 

### Option 2: Wrapped Factom Identities options parameters
```shell script
curl -X POST http://localhost:9080/1.0/create -H "Content-Type: application/json" -d \
'{
 "options": {
    "didVersion": "FACTOM_IDENTITY_CHAIN",
    "version": 1,
    "keys": [
      {
        "type": "idpub",
        "publicValue": "idpub2Cy86teq57qaxHyqLA8jHwe5JqqCvL1HGH4cKRcwSTbymTTh5n"
      }
    ],
    "tags": [
      "my-app",
      "more-info",
      "id-example-factom-identities"
    ],
    "networkName": "testnet"
  }
}'
```
The options are:
* didVersion - REQUIRED: Describes this mode (FACTOM_IDENTITY_CHAIN) or the full Factom DID specification mode (FACTOM_V1_JSON) .

* keys - REQUIRED: an array of keys/ippubs that will appear in the resulting DID document. These are the keys that will be used by the identity to sign VCs or presentations (see Key Objects below). In this mode only ED25519 public keys or Factom idpub addresses are allowed

* tags - REQUIRED: an array of strings that will be the external ids for the chain entry forming the basis of the DID. These do not have to be unique as the nonce will determine the unique DID URI.

* networkName - OPTIONAL: should be either "testnet" or "mainnet" and determines where the DID will be created.

## Build and Run (Maven)

```
mvn clean install
```

## Build and Run (Docker)

```
docker build -f ./Dockerfile . -t sphereon/uni-registrar-driver-did-factom
docker run -p 9080:9080 sphereon/uni-registrar-driver-did-factom
```
## Driver Environment Variables

The driver recognizes the following environment variables:

### `uniregistrar_driver_did_factom_exampleSetting`

* An example setting for the driver.
* DidController value: (empty string)

## Driver Metadata

The driver returns the following metadata in addition to a DID document:

* `exampleMetadata`: Example metadata
