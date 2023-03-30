Microservice with Saga Example...


Success Test URL 

```
http://localhost:8181/orders 
```

Payload

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

----------------------------------

