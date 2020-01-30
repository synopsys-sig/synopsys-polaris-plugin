# Configuring

Use the following process to configure ${solution_name} plugin:

1. After installing ${solution_name} in Jenkins, navigate to *Manage Jenkins > Configure System*.
1. Scroll down to the ${plugin_config_header} section and complete the following:
    * *Polaris URL*: The URL to your ${polaris_product_name} instance.
    * *Polaris credentials*: The Jenkins secret text credentials that you have created to store your ${polaris_product_name} access token.
    * *Advanced*: If you want to change the timeout that ${solution_name} will use when waiting
for a response from the ${polaris_product_name} server, click *Advanced*
and enter the timeout in seconds in the *Polaris connection timeout* field.
    * *Test connection to Polaris*: To test that ${solution_name} can connect to ${polaris_product_name}
with the configuration data you have entered, click *Test connection to Polaris*.
