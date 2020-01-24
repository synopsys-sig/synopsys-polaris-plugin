# Configuring

Use the following process to configure ${solution_name} plugin:

1. After installing ${solution_name} in Jenkins, navigate to *Manage Jenkins > Configure System*.
1. Scroll down to the ${plugin_config_header} section and complete the following:
    * *Polaris URL*: The URL to your ${polaris_product_name} instance.
    * *Polaris credentials*: ${solution_name} only supports secret text credentials. The secret text is your ${polaris_product_name} user token.
If you have already created a secret text credentials item in Jenkins for your ${polaris_product_name} user token, select it from the drop-down selector.
Otherwise, click *Add > Jenkins*, and in the *Add Credentials* screen, select *Kind > Secret text*.
Enter your ${polaris_product_name} user token in the *Secret* field, enter a name in the *ID* field,
and optionally enter a description in the *Description* field. Click *Add*,
which will return you to the *${plugin_config_header}* section of the *Configure System* screen.
Select the name (ID) of the credentials item you just created using the drop-down selector.
    * *Advanced*: If you want to change the timeout that ${solution_name} will use when waiting
for a response from the ${polaris_product_name} server, click *Advanced*
and enter the timeout in seconds in the *Polaris connection timeout* field.
    * *Test connection to Polaris*: To test that ${solution_name} can connect to ${polaris_product_name}
with the configuration data you have entered, click *Test connection to Polaris*.
