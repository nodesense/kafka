Microservice with Saga Example...

Install Postman

```
sudo snap install postman

postman
```

Success Test URL 

```
http://localhost:8181/orders
```

Payload for POSTMAN or save the content into order-success.json and use with Curl. refer commands for curl below..

```
{
    "customerId": "d215b5f8-0249-4dc5-89a3-51fd148cfb41",
    "restaurantId": "d215b5f8-0249-4dc5-89a3-51fd148cfb45",
    "address": {
        "street": "1st main road",
        "postalCode": "560001",
        "city" : "Bengaluru"
    },
    "price": 200.00,
    "items": [
        {
            "productId": "d215b5f8-0249-4dc5-89a3-51fd148cfb48",
            "quantity": 1,
            "price": 50.00,
            "subTotal": 50.00
        },
        {
            "productId": "d215b5f8-0249-4dc5-89a3-51fd148cfb48",
            "quantity": 3,
            "price": 50.00,
            "subTotal": 150.00
        }
    ]
}
```

Web service reply with tracking id, use this id for getting status of the order using below end point

```
{
    "orderTrackingId": "2e9056e0-ff1d-4187-8687-8e9ac494fba0",
    "orderStatus": "PENDING",
    "message": "Order created successfully"
}
```

POSTMAN GET METHOD , note, ID will vary request to request..

```
http://localhost:8181/orders/2e9056e0-ff1d-4187-8687-8e9ac494fba0
```

----------------------------------

## CURL

```
curl -X POST -H "Content-Type: application/json" -d order-success.json http://localhost:8181/orders

```

## Failure

Order a product that doesn't exit.


```
{
    "customerId": "d215b5f8-0249-4dc5-89a3-51fd148cfb41",
    "restaurantId": "d215b5f8-0249-4dc5-89a3-51fd148cfb45",
    "address": {
        "street": "1st main road",
        "postalCode": "560001",
        "city" : "Bengaluru"
    },
    "price": 25.00,
    "items": [
        {
            "productId": "d215b5f8-0249-4dc5-89a3-51fd148cfb47",
            "quantity": 1,
            "price": 25.00,
             "subTotal": 25.00
        }
    ]
}
```
