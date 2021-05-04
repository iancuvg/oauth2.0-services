# oauth2.0-services

First of all, I had to implement a local register/login.
This kind of implementation has a work flow just like that:

Registering:
	1. *clicks Sign Up button*
	2. redirects to localhost:8081/register
	3. *completes the register form and pressing the register button*
	4. it will create a POST request in which the user info is stored
	5. the user info will be checked in database, and:
		-> if the user already exists, the application will return a corresponding message. In this case, the message would look like this: "Email Address already in use!"
		-> if the user does not exist, the application will save the user in database, and will return a corresponding message. In this case, the message would look like this: "User registered successfully"
	
	Note: If someone who is going to break this database and look for accounts, all the users have the password encrypted.
	
Logging in:
	1. *completes the login form and pressing the login button*
	2. it will create a POST request in which the user info is stored
	3. using the Spring Security API, the POST request is going to be checked in the database, and:
		-> if the user sent a wrong POST request (wrong email/password), the application will return a corresponding message. In this case, the message would look like that: "Login failed: invalid email/password"
		-> if the user sent a good POST request, the application will generate a JSON Web Token based on user info (user details and roles which has on the web page). A session will be created in which the token and the user info will be saved. After this, the user can navigate to home page.
		
In the second, I had to implement a social login, using Google's Identity Provider.
This kind of implementation has a work flow just like that:

Logging in:
	1. "press the G button to use social login"
	2. before redirecting to the Google's IAM solution:
		-> it will save the authorization request, also the app will store the OAuth2 authorization request and redirect uri of the Angular client in the cookies
		
		-> after being redirected, the authorization request is getting loaded in the removeAuthorizationRequest method. This way, it will continue the process of attempting to authenticate, implemented in OAuth2LoginAuthenticationFilter. You'll see in the OAuth2LoginAuthenticationFilter that an authentication request is going to be created.
		
               -> This authentication request is going to be converted to an OAuth2 access token response, and this kind of authentication request holds a bunch of token response parameters:
            		1. access_token = parameter in which the authorization server accesses the token
            		2. token_type = specifies the type of token. This type of token is assigned by the authorization server.
            		3. scope = restricts the client app. Let's say, the client app may be granted READ and WRITE access to protected resources, or just READ access. this is also a good reason for implementing a custom Identity Provider...
            		4. id_token = it is a JSON Web Token which contains claims about the user authentication and other claims
            		5. expires_in = access token expiry
               
		-> After that, we must fetch the user's details from the OAuth2 access token response, and process them for user registration. If a user with the same email already exists in my database, this user will have his details updated otherwise, the app will register a new user. This process is explained in the processUserRegistration method.
		-> After that, we need to fetch the redirect uri sent by the Angular client, and checks if it validates against a list of allowed URIs mentioned in the application.properties section.
		If the URI is valid, the redirect will happen, with the authentication token added as a JWT format message.
		
In the third, I want to create a custom Identity Provider.
I would like to create this custom service, because I don't want to depend on facebook, or google, or whatever identity provider already exists. Let's say that I have to build this service as an enterprise solution for a company who would like to use their services internally, and not let an external provider know more than should.
