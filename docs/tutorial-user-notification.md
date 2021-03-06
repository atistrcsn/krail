#Introduction
Notifying users with messages seems a small topic, and typical code contains numerous calls to message boxes of one form or another.  Vaadin provides the ```Notification``` specifically for that purpose.

Consistency, however, can easily be lost, especially when there is a need for I18N as well.  There are also times when you want the message to go to more than one place - for example both a splash message, and repeated in the message bar at the bottom of the screen as you have already seen.
 
Krail provides a mechanism to support any method of presenting the message, but the message despatch is always from the ```UserNotifier```

<div class="admonition note">
<p class="first admonition-title">Note</p>
<p class="last">There is an open task #403 to provide options for the user to control how they receive notifications</p>
</div>


#Sending the Message

- Make the ```UserNotifier``` available to the ```NewsView``` by injecting it into the constructor
- Add the *sendNotificationBtn* button
- Set the button's click listener to despatch the notification with a call to ```userNotifier.notifyError```. There are warning and information calls available as well.
- add the button to the ```VerticalLayout``` in the call to ```setCentreCell```

```
package com.example.tutorial.pages;

import com.example.tutorial.i18n.LabelKey;
import com.google.inject.Inject;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import uk.q3c.krail.i18n.Translate;
import uk.q3c.krail.core.navigate.Navigator;
import uk.q3c.krail.core.user.notify.UserNotifier;
import uk.q3c.krail.core.view.Grid3x3ViewBase;
import uk.q3c.krail.core.view.component.ViewChangeBusMessage;

public class NewsView extends Grid3x3ViewBase {

    private Navigator navigator;
    private UserNotifier userNotifier;

    @Inject
    protected NewsView(Translate translate,Navigator navigator, UserNotifier userNotifier) {
        super(translate);
        this.navigator = navigator;
        this.userNotifier = userNotifier;
    }

    @Override
    protected void doBuild(ViewChangeBusMessage busMessage) {
        super.doBuild(busMessage);
        Button navigateToContactUsBtn = new Button("Contact Us");
        Button navigateToPrivatePage = new Button("Accounts");
        Button sendNotificationBtn = new Button("Send notification");
        navigateToContactUsBtn.addClickListener(c -> navigator.navigateTo("contact-us"));
        navigateToPrivatePage.addClickListener(c -> navigator.navigateTo("private/finance-department/accounts"));
        sendNotificationBtn.addClickListener((c -> userNotifier.notifyError(LabelKey.Do_Not_do_That)));
        setCentreCell(new VerticalLayout(navigateToContactUsBtn, navigateToPrivatePage, sendNotificationBtn));
    }
}
```
#Current methods of presentation
If you look at the constructor for ```TutorialUI``` you will see the ```MessageBar``` and ```VaadinNotification``` instances being injected.  The ```MessageBar``` is the component presented at the bottom of the screen, and ```VaadinNotification``` is a wrapper for the Vaadin Notification class.  Both just listen for notification messages via the Event Bus
```
  @Inject
    protected TutorialUI(Navigator navigator, ErrorHandler errorHandler, ConverterFactory converterFactory, ApplicationLogo logo, ApplicationHeader header,
                         UserStatusPanel userStatusPanel, UserNavigationMenu menu, UserNavigationTree navTree, Breadcrumb breadcrumb, SubPagePanel subpage,
                         MessageBar messageBar, Broadcaster broadcaster, PushMessageRouter pushMessageRouter, ApplicationTitle applicationTitle, Translate
                                     translate, CurrentLocale currentLocale, I18NProcessor translator, LocaleSelector localeSelector, VaadinNotification
                                     vaadinNotification, Option option) {
```
- Run the application and go to the "News Page", press the "Send Notification" button, and the message will appear as a Vaadin 'Splash' notification and in the message bar at the bottom of the screen.

#Different methods of presentation
If you wanted to provide your own methods of presenting user notifications, it is very easy to do, while still keeping the consistency of a single despatch point for user notifications - just copy the structure of ```DefaultVaadinNotification``` and provide your own method of presenting the messages. 

#Summary
At first this seems an almost trivial topic, but we would strongly recommend using ```UserNotifier``` from the start.  This will give you consistency, and enable a very quick and simple change of notification method(s) later.  

#Download from GitHub
To get to this point straight from GitHub:

```bash
git clone https://github.com/davidsowerby/krail-tutorial.git
cd krail-tutorial
git checkout --track origin/krail_0.10.0.0

```

Revert to commit *User Notification Complete*
