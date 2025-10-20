# 🔍 Debug Guide for 403 Error on POST /api/otc-orders

## ✅ Debugging Code Added

### 1. SecurityConfig Logging (SecurityConfig.java)
- Constructor prints: `🟢🟢🟢 SecurityConfig LOADED! 🟢🟢🟢`
- FilterChain method prints: `🔧 Building SecurityFilterChain...`
- After build prints: `✅ SecurityFilterChain built successfully!`
- Confirms that `/api/otc-orders/**` is set to `.permitAll()`

### 2. Request Logging Filter (RequestLoggingFilter.java) - NEW FILE
- Logs every incoming HTTP request with:
  - Method (GET, POST, etc.)
  - URI path
  - Query parameters
  - Remote address
- Look for: `📨 === INCOMING REQUEST ===`

### 3. Controller Constructor Logging (OtcOrderController.java)
- Constructor prints: `🟢🟢🟢 OtcOrderController LOADED! 🟢🟢🟢`
- Confirms Spring detected and loaded the controller
- Prints mapping: `🟢 Mapped to: /api/otc-orders`

### 4. Enhanced Endpoint Logging (OtcOrderController.java)
- **POST /api/otc-orders** prints:
  - `🔵 POST /api/otc-orders HIT!`
  - Request details (customerId, paymentMethod, items count)
  - Success: `✅ Order created successfully!`
  - Errors: `❌ [ExceptionType]: [message]` with stack trace

### 5. Test Endpoint (OtcOrderController.java)
- **GET /api/otc-orders/test** - NEW
- Simple connectivity test
- Returns: "OTC Orders endpoint is working!"
- Prints: `🟢 TEST endpoint hit - Controller is working!`

### 6. Spring Security Debug Logging (application.properties)
- Already enabled: `logging.level.org.springframework.security=DEBUG`
- Will show detailed security filter chain processing

---

## 📋 Testing Steps

### Step 1: Restart Application
1. Stop your Spring Boot application
2. Start it again
3. **Look for these messages in console:**
   ```
   🟢🟢🟢 SecurityConfig LOADED! 🟢🟢🟢
   🔧 Building SecurityFilterChain...
   ✅ SecurityFilterChain built successfully!
   ✅ /api/otc-orders/** is set to .permitAll()
   🟢🟢🟢 OtcOrderController LOADED! 🟢🟢🟢
   🟢 Mapped to: /api/otc-orders
   ```

### Step 2: Test GET Endpoint (Connectivity Check)
**Request:**
```http
GET http://localhost:8080/api/otc-orders/test
```

**Expected Console Output:**
```
📨 === INCOMING REQUEST ===
📨 Method: GET
📨 URI: /api/otc-orders/test
...
🟢 TEST endpoint hit - Controller is working!
```

**Expected Response:**
```
Status: 200 OK
Body: "OTC Orders endpoint is working!"
```

### Step 3: Test POST Endpoint (Order Creation)
**Request:**
```http
POST http://localhost:8080/api/otc-orders
Content-Type: application/json

{
  "customerId": 9,
  "paymentMethod": "CARD",
  "items": [
    {
      "otcProductId": 3,
      "pharmacyId": 1,
      "quantity": 2
    }
  ]
}
```

**Expected Console Output:**
```
📨 === INCOMING REQUEST ===
📨 Method: POST
📨 URI: /api/otc-orders
...
🔵 POST /api/otc-orders HIT!
🔵 Request received: OrderRequestDTO(...)
🔵 Customer ID: 9
🔵 Payment Method: CARD
🔵 Items count: 1
✅ Order created successfully!
```

**If 403 Error Occurs - Console Should Show:**
```
📨 === INCOMING REQUEST ===
📨 Method: POST
📨 URI: /api/otc-orders
[Spring Security DEBUG logs showing why request was denied]
```

---

## 🔍 What to Look For

### ✅ If Controller is Working:
- You'll see `🔵 POST /api/otc-orders HIT!` in console
- Request reached the controller
- Problem is likely in service/repository layer

### ❌ If Getting 403 Before Controller:
- You'll see `📨 === INCOMING REQUEST ===` but NOT `🔵 POST...`
- Request is blocked by security filter
- Look at Spring Security DEBUG logs between the two messages

### ❌ If Not Seeing RequestLoggingFilter:
- Filter might not be registered
- Check if `@Component` annotation is present on `RequestLoggingFilter`

### ❌ If Not Seeing Controller Constructor Message:
- Controller didn't load
- Check for compilation errors
- Check component scanning configuration

---

## 🛠️ Possible Solutions Based on Logs

### Scenario 1: No logs at all
**Problem:** Application not starting
**Check:** Compilation errors, missing dependencies

### Scenario 2: SecurityConfig loaded but no controller
**Problem:** Controller not being scanned
**Solution:** Check `@SpringBootApplication` location, ensure it's in parent package

### Scenario 3: Request logged but no controller hit
**Problem:** Security blocking request
**Solution:** Check Spring Security DEBUG logs for exact rejection reason

### Scenario 4: Controller hit but exception in service
**Problem:** Business logic error
**Solution:** Check stack trace from controller logging

---

## 📝 Next Steps

1. **Restart application** and copy ALL startup logs
2. **Test GET /api/otc-orders/test** first
3. **Test POST /api/otc-orders** with your request
4. **Copy complete console output** showing all emoji-marked logs
5. Share the output to identify the exact problem

---

## 🔧 Additional Debugging (If Still 403)

If still getting 403 after above steps, try:

1. **Check for method-level security:**
   ```bash
   grep -r "@Secured\|@PreAuthorize" src/
   ```

2. **Verify no duplicate SecurityFilterChain:**
   ```bash
   grep -r "SecurityFilterChain" src/
   ```

3. **Test with CSRF disabled:**
   Already disabled in SecurityConfig

4. **Check CORS preflight:**
   Try with Postman/Insomnia instead of browser

5. **Verify database constraints:**
   Check if customerId=9, pharmacyId=1, otcProductId=3 exist
