# Chat Translator

🅰️->🈯️ The Chat Translator plugin allows players to translate any message in the chat box by right clicking the message. The plugin uses Google Cloud Platform's Translate API. This means the player must setup such an account and generate an API key for use in this plugin. Steps are given below.

:warning: **You will need to set up a billing account on Google. You will almost certainly never be charged by Google for normal use of this plugin.** :warning:

# How do I use this plugin?
---
  - In the RuneLite Chat Translator panel, you can specify a source language and a target language for translation.
    - The source language is a language you speak
    - The target language is a language you (probably) don't speak and want to translate to
  - Right click over a chat message of text in the chat box to translate
    ![Demo 1](https://imbleau.com/runelite/demo1.gif)

  - You can also type a message, and right click your chat input to translate

    ![Demo 2](https://imbleau.com/runelite/demo2.gif)

# Setup
---
  1. Start a Google Cloud Platform account.
     - Option 1 **(Recommended)** - [Use my referral link](http://imbleau.com/google-refer) to help me. Thank you!
     - Option 2 - [Sign up the default way](https://console.cloud.google.com/freetrial) to help Google. :(
  
     ![Step 1](https://imbleau.com/runelite/step1.png)

  2. Navigate to [Google Cloud Platform Billing](https://console.cloud.google.com/billing) and set up a billing account.
    
     ![Step 2](https://imbleau.com/runelite/step2.png)

  3. Link your [billing account](https://console.cloud.google.com/billing) to a project. You are automatically started with a created project when you sign up called "My Project". If you don't have a created, project, you can [click here](https://console.cloud.google.com/projectcreate) to create one.
    
     ![Step 3](https://imbleau.com/runelite/step3.png)
     - Will translating cost you? **Short answer: Probably not.**
     At the time of writing this, you receive 500,000 characters of translation free every month. But their [pricing](https://cloud.google.com/translate/pricing) may change, so check. Unless you plan on translating that much, you pay __nothing__. If you used my referral link, you were also gifted the amount of 18,000,000 characters of translation for the first 3 months for free.

        | Monthly Usage | Price |
        | ------ | ----- |
        | < 500,000 characters | Free (applied as $10 credit every month) |
        | > 500,000 characters | $20 per million characters |

  4. [Enable the Google Cloud Translate API](https://console.cloud.google.com/flows/enableapi?apiid=translate.googleapis.com).
    
     ![Step 4](https://imbleau.com/runelite/step4.png)
     - If you ever want to stop this plugin from working, you can [shut down the project](https://cloud.google.com/resource-manager/docs/creating-managing-projects#shutting_down_projects), [close your billing account](https://cloud.google.com/billing/docs/how-to/manage-billing-account#close_a_billing_account), or [disable the API](https://cloud.google.com/service-usage/docs/enable-disable#disabling).
    - Alternatively, if you're ever worried about getting charged, you can [set a budget](https://cloud.google.com/billing/docs/how-to/budgets) with notifications or [cap API usage](https://cloud.google.com/apis/docs/capping-api-usage) with quoatas. 
    
    
  5. Navigate to the [API credentials](https://console.cloud.google.com/apis/credentials) of your project.
    
     ![Step 5](https://imbleau.com/runelite/step5.png)

  6. [Create an API Key for your account](https://console.cloud.google.com/apis/credentials/key).
     - **(OPTIONAL)** *If you want to be more safe with your API Key, you can restrict the usage of the API key to the Cloud Translate API only.*
     
     ![Step 6](https://imbleau.com/runelite/step6.png)
    
  7. [Copy your API Key](https://console.cloud.google.com/apis/credentials) which will be input to the plugin.
    
     ![Step 7](https://imbleau.com/runelite/step7.png)

  8. On the RuneLite plugin, open the Chat Translator panel, and press "Authenticate". Enter your API Key received in step 7.
    
     ![Step 8](https://imbleau.com/runelite/step8.png)
     
  - You're done! Enjoy!

    ![Step 8](https://imbleau.com/runelite/done.png)
