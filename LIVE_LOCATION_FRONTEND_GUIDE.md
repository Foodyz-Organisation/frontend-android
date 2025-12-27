# Frontend Implementation Guide - Live Location Tracking

## Complete Implementation Roadmap

This guide covers everything you need to implement on the frontend, from professional location management to user live tracking with WebSocket integration.

---

## Part 1: Professional Side - Location Management

### 1.1 Add/Edit Locations Screen

**UI Components Needed**:
- Map view with marker placement
- Location list (show all saved locations)
- Add/Edit location form
- Delete location button

**API Endpoint**:
```
PATCH /professionalaccount/:professionalId
```

**Request Body**:
```json
{
  "locations": [
    {
      "name": "Main Branch",
      "address": "123 Main Street, Tunis",
      "lat": 36.8065,
      "lon": 10.1815
    },
    {
      "name": "Downtown Location",
      "address": "456 Avenue Habib Bourguiba",
      "lat": 36.8500,
      "lon": 10.2000
    }
  ]
}
```

**Example Code (React/TypeScript)**:
```typescript
interface Location {
  name?: string;
  address?: string;
  lat: number;
  lon: number;
}

// Component: LocationManager.tsx
const LocationManager = () => {
  const [locations, setLocations] = useState<Location[]>([]);
  const [isAddingLocation, setIsAddingLocation] = useState(false);

  // Load existing locations
  useEffect(() => {
    fetchProfessionalProfile().then(profile => {
      setLocations(profile.locations || []);
    });
  }, []);

  // Add new location
  const addLocation = async (newLocation: Location) => {
    const updatedLocations = [...locations, newLocation];
    
    await fetch(`/professionalaccount/${professionalId}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ locations: updatedLocations })
    });
    
    setLocations(updatedLocations);
  };

  // Delete location
  const deleteLocation = async (index: number) => {
    const updatedLocations = locations.filter((_, i) => i !== index);
    
    await fetch(`/professionalaccount/${professionalId}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ locations: updatedLocations })
    });
    
    setLocations(updatedLocations);
  };

  return (
    <div>
      <h2>Service Locations</h2>
      
      {/* Location List */}
      {locations.map((loc, index) => (
        <LocationCard
          key={index}
          location={loc}
          onDelete={() => deleteLocation(index)}
        />
      ))}
      
      {/* Add Location Button */}
      <button onClick={() => setIsAddingLocation(true)}>
        + Add New Location
      </button>
      
      {/* Add Location Modal with Map */}
      {isAddingLocation && (
        <AddLocationModal
          onSave={addLocation}
          onClose={() => setIsAddingLocation(false)}
        />
      )}
    </div>
  );
};
```

**Map Integration (OpenStreetMap/Leaflet)**:
```typescript
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';

const AddLocationModal = ({ onSave, onClose }) => {
  const [selectedPosition, setSelectedPosition] = useState(null);
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');

  // Map click handler
  const LocationMarker = () => {
    useMapEvents({
      click(e) {
        setSelectedPosition(e.latlng);
        
        // Optional: Reverse geocode to get address
        reverseGeocode(e.latlng.lat, e.latlng.lng).then(addr => {
          setAddress(addr);
        });
      },
    });
    
    return selectedPosition ? (
      <Marker position={selectedPosition} />
    ) : null;
  };

  const handleSave = () => {
    if (!selectedPosition) return;
    
    onSave({
      name,
      address,
      lat: selectedPosition.lat,
      lon: selectedPosition.lng
    });
    
    onClose();
  };

  return (
    <div className="modal">
      <h3>Add Service Location</h3>
      
      <input
        placeholder="Location Name (e.g., Main Branch)"
        value={name}
        onChange={(e) => setName(e.target.value)}
      />
      
      <input
        placeholder="Address"
        value={address}
        onChange={(e) => setAddress(e.target.value)}
      />
      
      <p>Tap on map to select location</p>
      
      <MapContainer
        center={[36.8065, 10.1815]}
        zoom={13}
        style={{ height: '400px' }}
      >
        <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        <LocationMarker />
      </MapContainer>
      
      <button onClick={handleSave}>Save Location</button>
      <button onClick={onClose}>Cancel</button>
    </div>
  );
};
```

---

## Part 2: User Side - Viewing Professional Locations

### 2.1 Display Professional Locations on Profile

When user views a professional's profile/menu, show their service locations.

**API Call**:
```
GET /professionalaccount/:professionalId
```

**Response**:
```json
{
  "_id": "...",
  "fullName": "Restaurant Name",
  "locations": [
    {
      "name": "Main Branch",
      "address": "123 Main Street",
      "lat": 36.8065,
      "lon": 10.1815
    }
  ]
}
```

**Display Code**:
```typescript
const ProfessionalProfile = ({ professionalId }) => {
  const [professional, setProfessional] = useState(null);

  useEffect(() => {
    fetch(`/professionalaccount/${professionalId}`)
      .then(res => res.json())
      .then(data => setProfessional(data));
  }, [professionalId]);

  if (!professional) return <Loading />;

  return (
    <div>
      <h1>{professional.fullName}</h1>
      
      {/* Service Locations */}
      <section>
        <h3>Service Locations</h3>
        {professional.locations?.map((location, index) => (
          <div key={index} className="location-card">
            <h4>{location.name || `Location ${index + 1}`}</h4>
            <p>{location.address}</p>
            
            {/* Show on mini map */}
            <MiniMap lat={location.lat} lon={location.lon} />
            
            {/* Navigation button */}
            <button onClick={() => openNavigation(location.lat, location.lon)}>
              Get Directions
            </button>
          </div>
        ))}
      </section>
    </div>
  );
};

// Open in navigation app
const openNavigation = (lat: number, lon: number) => {
  // For web
  window.open(`https://www.google.com/maps/dir/?api=1&destination=${lat},${lon}`);
  
  // For mobile (Android/iOS)
  // window.location.href = `geo:${lat},${lon}`;
};
```

---

## Part 3: Order Creation with Location Context

### 3.1 Create Order with Optional ETA

When user creates delivery order, optionally include ETA.

**API Call**:
```
POST /orders
```

**Request Body**:
```json
{
  "userId": "user123",
  "professionalId": "pro456",
  "orderType": "delivery",
  "items": [...],
  "totalPrice": 25.50,
  "deliveryAddress": "789 Customer Street",
  "paymentMethod": "CARD",
  "notes": "Ring doorbell",
  
  // OPTIONAL: Initial ETA
  "estimatedArrivalMinutes": 20
}
```

**Example Code**:
```typescript
const OrderConfirmationScreen = () => {
  const [estimatedMinutes, setEstimatedMinutes] = useState<number | null>(null);

  const createOrder = async () => {
    const orderData = {
      userId: currentUser._id,
      professionalId: restaurant._id,
      orderType: 'delivery',
      items: cartItems,
      totalPrice: calculateTotal(),
      deliveryAddress: userAddress,
      paymentMethod: selectedPaymentMethod,
      notes: orderNotes,
      
      // Include ETA if user provided
      ...(estimatedMinutes && { estimatedArrivalMinutes: estimatedMinutes })
    };

    const response = await fetch('/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(orderData)
    });

    const order = await response.json();
    
    // Navigate to tracking screen
    navigateToTracking(order._id || order.order._id);
  };

  return (
    <div>
      <h2>Confirm Order</h2>
      
      {/* Delivery Address */}
      <input value={userAddress} onChange={...} />
      
      {/* Optional: ETA Input */}
      <div>
        <label>How long until you arrive?</label>
        <select onChange={(e) => setEstimatedMinutes(Number(e.target.value))}>
          <option value="">Select...</option>
          <option value="10">10 minutes</option>
          <option value="15">15 minutes</option>
          <option value="20">20 minutes</option>
          <option value="30">30 minutes</option>
        </select>
      </div>
      
      <button onClick={createOrder}>Place Order</button>
    </div>
  );
};
```

---

## Part 4: WebSocket Integration - Live Location Tracking

### 4.1 Setup WebSocket Connection

**Install Socket.IO Client**:
```bash
npm install socket.io-client
```

**Create WebSocket Service**:
```typescript
// services/OrderTrackingService.ts
import { io, Socket } from 'socket.io-client';

class OrderTrackingService {
  private socket: Socket | null = null;
  private watchId: number | null = null;

  // Connect to WebSocket
  connect() {
    this.socket = io('http://YOUR_BACKEND_URL/order-tracking', {
      transports: ['websocket'],
      reconnection: true
    });

    this.socket.on('connect', () => {
      console.log('Connected to order tracking');
    });

    this.socket.on('disconnect', () => {
      console.log('Disconnected from order tracking');
    });

    this.socket.on('error', (error) => {
      console.error('WebSocket error:', error);
    });

    return this.socket;
  }

  // Join order tracking room
  joinOrder(orderId: string, userType: 'user' | 'pro') {
    if (!this.socket) return;

    this.socket.emit('join-order', {
      orderId,
      userType
    });
  }

  // Listen for restaurant location
  onRestaurantLocation(callback: (location: any) => void) {
    if (!this.socket) return;
    
    this.socket.on('restaurant-location', callback);
  }

  // Start sharing user location
  startSharing(orderId: string, userId: string) {
    if (!this.socket) return;

    this.socket.emit('start-sharing', {
      orderId,
      userId
    });

    // Start GPS tracking
    this.startLocationTracking(orderId, userId);
  }

  // Stop sharing location
  stopSharing(orderId: string, userId: string) {
    if (!this.socket) return;

    this.socket.emit('stop-sharing', {
      orderId,
      userId
    });

    // Stop GPS tracking
    this.stopLocationTracking();
  }

  // Start GPS tracking and send updates
  private startLocationTracking(orderId: string, userId: string) {
    if (!navigator.geolocation) {
      alert('Geolocation not supported');
      return;
    }

    // Watch position (updates automatically as user moves)
    this.watchId = navigator.geolocation.watchPosition(
      (position) => {
        this.sendLocationUpdate(orderId, userId, {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
          accuracy: position.coords.accuracy
        });
      },
      (error) => {
        console.error('Geolocation error:', error);
      },
      {
        enableHighAccuracy: true,
        maximumAge: 5000, // 5 seconds
        timeout: 10000
      }
    );
  }

  // Stop GPS tracking
  private stopLocationTracking() {
    if (this.watchId !== null) {
      navigator.geolocation.clearWatch(this.watchId);
      this.watchId = null;
    }
  }

  // Send location update
  private sendLocationUpdate(
    orderId: string,
    userId: string,
    coords: { lat: number; lng: number; accuracy: number }
  ) {
    if (!this.socket) return;

    this.socket.emit('location-update', {
      orderId,
      userId,
      lat: coords.lat,
      lng: coords.lng,
      accuracy: coords.accuracy
    });
  }

  // Listen for location updates (for professional)
  onLocationUpdate(callback: (data: any) => void) {
    if (!this.socket) return;
    
    this.socket.on('location-update', callback);
  }

  // Set ETA
  setETA(orderId: string, userId: string, minutes: number) {
    if (!this.socket) return;

    this.socket.emit('set-eta', {
      orderId,
      userId,
      estimatedMinutes: minutes
    });
  }

  // Listen for ETA updates (for professional)
  onETAUpdate(callback: (data: any) => void) {
    if (!this.socket) return;
    
    this.socket.on('eta-update', callback);
  }

  // Disconnect
  disconnect() {
    this.stopLocationTracking();
    
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
  }
}

export default new OrderTrackingService();
```

---

## Part 5: User Tracking Screen

### 5.1 Order Tracking Screen (User Side)

**UI Components**:
- Map showing user's location and restaurant location
- Distance to restaurant
- ETA selector
- Start/Stop sharing button
- Order status

**Complete Implementation**:
```typescript
import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import OrderTrackingService from './services/OrderTrackingService';

const UserOrderTrackingScreen = ({ orderId, userId }) => {
  const [isSharing, setIsSharing] = useState(false);
  const [userLocation, setUserLocation] = useState(null);
  const [restaurantLocation, setRestaurantLocation] = useState(null);
  const [distance, setDistance] = useState(null);
  const [selectedETA, setSelectedETA] = useState<number | null>(null);

  useEffect(() => {
    // Connect to WebSocket
    OrderTrackingService.connect();

    // Join order room as user
    OrderTrackingService.joinOrder(orderId, 'user');

    // Listen for restaurant location
    OrderTrackingService.onRestaurantLocation((location) => {
      console.log('Restaurant location:', location);
      setRestaurantLocation(location);
    });

    // Listen for location updates (to get distance)
    OrderTrackingService.onLocationUpdate((data) => {
      if (data.userId === userId) {
        setDistance(data.distanceFormatted);
        setUserLocation({ lat: data.lat, lng: data.lng });
      }
    });

    // Cleanup
    return () => {
      OrderTrackingService.stopSharing(orderId, userId);
      OrderTrackingService.disconnect();
    };
  }, [orderId, userId]);

  const handleStartSharing = () => {
    OrderTrackingService.startSharing(orderId, userId);
    setIsSharing(true);
  };

  const handleStopSharing = () => {
    OrderTrackingService.stopSharing(orderId, userId);
    setIsSharing(false);
  };

  const handleSetETA = (minutes: number) => {
    OrderTrackingService.setETA(orderId, userId, minutes);
    setSelectedETA(minutes);
  };

  return (
    <div className="tracking-screen">
      <h2>Order Tracking</h2>

      {/* Distance Display */}
      {distance && (
        <div className="distance-card">
          <p>📍 Distance to restaurant: <strong>{distance}</strong></p>
        </div>
      )}

      {/* Map */}
      <MapContainer
        center={
          userLocation 
            ? [userLocation.lat, userLocation.lng]
            : restaurantLocation
            ? [restaurantLocation.lat, restaurantLocation.lon]
            : [36.8065, 10.1815]
        }
        zoom={15}
        style={{ height: '400px', width: '100%' }}
      >
        <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        
        {/* User marker */}
        {userLocation && (
          <Marker position={[userLocation.lat, userLocation.lng]}>
            <Popup>Your Location</Popup>
          </Marker>
        )}
        
        {/* Restaurant marker */}
        {restaurantLocation && (
          <Marker position={[restaurantLocation.lat, restaurantLocation.lon]}>
            <Popup>{restaurantLocation.name || 'Restaurant'}</Popup>
          </Marker>
        )}
      </MapContainer>

      {/* Location Sharing Controls */}
      <div className="controls">
        {!isSharing ? (
          <button onClick={handleStartSharing} className="btn-primary">
            📍 Start Sharing Location
          </button>
        ) : (
          <button onClick={handleStopSharing} className="btn-danger">
            ❌ Stop Sharing Location
          </button>
        )}
      </div>

      {/* ETA Selector */}
      <div className="eta-selector">
        <h3>Estimated Arrival Time</h3>
        <p>Let the restaurant know when you'll arrive</p>
        
        <div className="eta-buttons">
          {[5, 10, 15, 20, 30].map(minutes => (
            <button
              key={minutes}
              onClick={() => handleSetETA(minutes)}
              className={selectedETA === minutes ? 'active' : ''}
            >
              {minutes} min
            </button>
          ))}
        </div>

        {selectedETA && (
          <p className="eta-confirmation">
            ✅ ETA set to {selectedETA} minutes
          </p>
        )}
      </div>

      {/* Restaurant Info */}
      {restaurantLocation && (
        <div className="restaurant-info">
          <h4>{restaurantLocation.name}</h4>
          <p>{restaurantLocation.address}</p>
          
          <button onClick={() => openNavigation(restaurantLocation.lat, restaurantLocation.lon)}>
            🗺️ Open in Maps
          </button>
        </div>
      )}
    </div>
  );
};

const openNavigation = (lat: number, lon: number) => {
  window.open(`https://www.google.com/maps/dir/?api=1&destination=${lat},${lon}`);
};

export default UserOrderTrackingScreen;
```

---

## Part 6: Professional Tracking Screen

### 6.1 Professional Dashboard - Track User

**UI Components**:
- Map showing user's real-time location
- Distance to restaurant
- User's ETA
- Order status controls

**Complete Implementation**:
```typescript
const ProfessionalOrderTrackingScreen = ({ orderId, professionalId }) => {
  const [userLocation, setUserLocation] = useState(null);
  const [restaurantLocation, setRestaurantLocation] = useState(null);
  const [distance, setDistance] = useState(null);
  const [userETA, setUserETA] = useState<number | null>(null);
  const [isSharingActive, setIsSharingActive] = useState(false);

  useEffect(() => {
    // Connect to WebSocket
    OrderTrackingService.connect();

    // Join order room as professional
    OrderTrackingService.joinOrder(orderId, 'pro');

    // Listen for restaurant location (your own location)
    OrderTrackingService.onRestaurantLocation((location) => {
      setRestaurantLocation(location);
    });

    // Listen for user location updates
    OrderTrackingService.onLocationUpdate((data) => {
      console.log('User location update:', data);
      
      setUserLocation({ lat: data.lat, lng: data.lng });
      setDistance(data.distanceFormatted);
    });

    // Listen for sharing status
    const socket = OrderTrackingService.connect();
    socket.on('sharing-started', () => {
      setIsSharingActive(true);
    });
    socket.on('sharing-stopped', () => {
      setIsSharingActive(false);
    });

    // Listen for ETA updates
    OrderTrackingService.onETAUpdate((data) => {
      console.log('User ETA:', data.estimatedMinutes);
      setUserETA(data.estimatedMinutes);
    });

    return () => {
      OrderTrackingService.disconnect();
    };
  }, [orderId]);

  return (
    <div className="pro-tracking-screen">
      <h2>Order Tracking</h2>

      {/* Status Banner */}
      {isSharingActive ? (
        <div className="status-active">
          ✅ Customer is sharing live location
        </div>
      ) : (
        <div className="status-inactive">
          ⏳ Waiting for customer to share location
        </div>
      )}

      {/* Distance & ETA */}
      {distance && (
        <div className="tracking-info">
          <div className="info-card">
            <span>📍 Distance</span>
            <strong>{distance}</strong>
          </div>
          
          {userETA && (
            <div className="info-card">
              <span>⏱️ Customer ETA</span>
              <strong>{userETA} minutes</strong>
            </div>
          )}
        </div>
      )}

      {/* Map */}
      <MapContainer
        center={
          restaurantLocation 
            ? [restaurantLocation.lat, restaurantLocation.lon]
            : [36.8065, 10.1815]
        }
        zoom={15}
        style={{ height: '500px' }}
      >
        <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        
        {/* Restaurant marker (your location) */}
        {restaurantLocation && (
          <Marker position={[restaurantLocation.lat, restaurantLocation.lon]}>
            <Popup>🏪 Your Restaurant</Popup>
          </Marker>
        )}
        
        {/* User marker (customer) */}
        {userLocation && (
          <Marker position={[userLocation.lat, userLocation.lng]}>
            <Popup>
              🚶 Customer
              {userETA && <p>ETA: {userETA} min</p>}
            </Popup>
          </Marker>
        )}
      </MapContainer>
    </div>
  );
};

export default ProfessionalOrderTrackingScreen;
```

---

## Part 7: Navigation Flow

### Complete User Journey

```
1. Professional Setup
   ├─ Professional opens "Locations" settings
   ├─ Taps on map to add location(s)
   ├─ Saves location with name & address
   └─ Backend: PATCH /professionalaccount/:id { locations: [...] }

2. User Browses Menu
   ├─ User views professional profile
   ├─ Sees professional's locations on map
   └─ Backend: GET /professionalaccount/:id

3. User Places Order
   ├─ Adds items to cart
   ├─ Selects "Delivery" order type
   ├─ Enters delivery address
   ├─ (Optional) Selects ETA "I'll be there in 15 min"
   ├─ Confirms order
   └─ Backend: POST /orders { ..., estimatedArrivalMinutes: 15 }

4. User Tracks Order (Live Location)
   ├─ Navigate to Order Tracking Screen
   ├─ WebSocket: connect to /order-tracking
   ├─ WebSocket: emit 'join-order' { orderId, userType: 'user' }
   ├─ WebSocket: receive 'restaurant-location'
   ├─ Show map with restaurant marker
   ├─ User taps "Start Sharing Location"
   ├─ WebSocket: emit 'start-sharing'
   ├─ GPS starts tracking → emit 'location-update' every 5 sec
   ├─ User selects ETA → emit 'set-eta' { estimatedMinutes: 15 }
   └─ Map updates in real-time

5. Professional Tracks User
   ├─ Professional opens order details
   ├─ Navigate to Tracking Screen
   ├─ WebSocket: emit 'join-order' { orderId, userType: 'pro' }
   ├─ WebSocket: receive 'location-update' (user's position)
   ├─ WebSocket: receive 'eta-update' (user's ETA)
   ├─ Map shows user moving in real-time
   └─ Distance updates automatically
```

---

## Part 8: Quick Start Checklist

### Professional Side
- [ ] Create location management UI (map + form)
- [ ] Implement `PATCH /professionalaccount/:id` to save locations
- [ ] Display locations on professional profile
- [ ] Test: Add 2+ locations and verify they save

### User Side - Order Creation
- [ ] Add ETA selector to order confirmation screen
- [ ] Include `estimatedArrivalMinutes` in order payload (optional)
- [ ] Test: Create order with and without ETA

### User Side - Live Tracking
- [ ] Install `socket.io-client`
- [ ] Create `OrderTrackingService.ts`
- [ ] Build User Tracking Screen with map
- [ ] Implement "Start/Stop Sharing" buttons
- [ ] Implement ETA selector (5, 10, 15, 20, 30 min buttons)
- [ ] Request location permissions
- [ ] Test: Share location and verify updates

### Professional Side - Tracking
- [ ] Build Professional Tracking Screen
- [ ] Listen for user location updates
- [ ] Display distance and ETA
- [ ] Show user marker on map
- [ ] Test: Track user movement in real-time

---

## Part 9: Testing Scenarios

### Test 1: Professional Adds Locations
1. Open professional settings
2. Add 3 different locations
3. Verify all 3 appear in GET /professionalaccount/:id response
4. View professional profile → see all 3 locations on map

### Test 2: User Places Order with ETA
1. Add items to cart
2. Select delivery
3. Set ETA to "15 minutes"
4. Place order
5. Verify order response includes `estimatedArrivalMinutes: 15`

### Test 3: Live Location Tracking
1. User creates delivery order
2. Navigate to tracking screen
3. Tap "Start Sharing Location"
4. Move phone/device
5. Professional dashboard → see user marker moving
6. Verify distance updates in real-time

### Test 4: ETA Updates
1. User in tracking screen
2. Tap "10 minutes" ETA button
3. Professional dashboard → verify shows "Customer ETA: 10 minutes"
4. User taps "20 minutes"
5. Professional dashboard → updates to "20 minutes"

---

## Part 10: Important Notes

### Permissions
- **Location Permission**: Request GPS permission before starting tracking
- **Background Location** (optional): For continued tracking when app in background

### Performance
- **Update Frequency**: Send location updates every 5-10 seconds (not every second)
- **Battery**: Use `enableHighAccuracy: true` only when actively delivering
- **Network**: WebSocket auto-reconnects if connection lost

### Error Handling
```typescript
// Handle location permission denied
navigator.geolocation.watchPosition(
  (position) => { /* success */ },
  (error) => {
    if (error.code === error.PERMISSION_DENIED) {
      alert('Please enable location services');
    }
  }
);

// Handle WebSocket disconnection
socket.on('disconnect', () => {
  showToast('Connection lost. Reconnecting...');
});
```

### Security
- Only share location for active delivery orders
- Stop sharing when order is complete/cancelled
- Validate user owns the order before allowing tracking

---

## Summary

**Total Implementation**:
1. **Professional**: Location management (1 screen)
2. **User**: Order with ETA (1 form field)
3. **User**: Live tracking (1 screen + WebSocket service)
4. **Professional**: Track user (1 screen + WebSocket listeners)

**Backend**: Already 100% ready ✅  
**Frontend**: ~3-4 screens + 1 WebSocket service

You now have everything you need to implement the complete live location tracking feature on your frontend!
