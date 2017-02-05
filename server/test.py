#!/usr/bin/python

import json
import googlemaps
from datetime import datetime

gmaps = googlemaps.Client(key='AIzaSyA-S7NFs0U7prOtOHVF558VSL51qOlOmd0')

## Geocoding an address
geocode_result = gmaps.geocode('DC Library, Ring Road, Waterloo, ON, Canada')
#print geocode_result[0]

## Look up an address with reverse geocoding
#reverse_geocode_result = gmaps.reverse_geocode((40.714224, -73.961452))
#
## Request directions via public transit
#now = datetime.now()
#directions_result = gmaps.directions("Sydney Town Hall",
#                                     "Parramatta, NSW",
#                                     mode="transit",
#                                     departure_time=now)
#
place = geocode_result[0]
ret = gmaps.places_nearby(radius=100, type="restaurant", location=place['geometry']['location'].values())
print ret
