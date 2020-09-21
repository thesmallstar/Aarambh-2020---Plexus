# Plexus

The use of Internet shutdowns as a tool for political purposes has steadily risen. Driven largely by political and national security concerns, state-ordered Internet shutdowns have become typical in many countries. Plexus is an Android App that resorts to this very concern and enables offline emergency communication with support for multimedia messages including images, videos and audio recordings.

## Getting Started

To get this project up and running on your local machine, all you need to do is clone or download this project, set up [Firebase Console](https://firebase.google.com/docs/android/setup), Open this project in android studio and run it on any android device or emulator.

### Prerequisites

A google account to set up firebase.

## Using the Application

On the very first screen, the users are asked to signin/signup. The next screen gives them three important utilities.

1. Send Quick emergency text: This feature provides users __SOS services in network as well as no network conditions.__ 
The user is given an option to choose his emergency. Now if the device has access to mobile network it sends the users coordinates to the respective official. But the device is not connected to any network, which is a very natural case at times of natural calamities etc, __the device builds a peer to peer chaining mesh network using ultrasonic sound waves.__ The message travels through the network until it finds a device with network connectivity and the broadcasts the message. Each node acts as a reciever and broadcasts the message forward, all this is done through sound waves. Alternatively, the user also gets an option to send personalised text messages using the same technology.

2. Walkie Talkie feature: Our app connects to nearby devices in a __fully-offline__ peer-to-peer manner. Connections between devices are __high-bandwidth, low-latency, and fully encrypted to enable fast, secure data transfers__. A primary goal of this module is to provide a platform that is simple, reliable, and performant. Under the hood, the app uses a combination of __Bluetooth, BLE, and Wifi hotspots,__ leveraging the strengths of each while circumventing their respective weaknesses. As a convenience, users are not prompted to turn on Bluetooth or Wi-Fi — the app enables these features as they are required, and restores the device to its prior state once the app is done using the API, ensuring a smooth user experience. Everyone doesn't carry a walkie-talkie with them, so we created a walkie-talkie of something you carry everyday. This can be used during emergencies to __comunicate without any network__ or for personal comunication in a crowded area when network is banned. We plan to extend this to a chain network where messages can be relayed over much longer distances than current.

3. Public Emergency feature: The idea here is to provide a service which can use widespread public networks to help people in emergency situtions. Consider a situation where internet is __banned over an area which is connected within a wide area network__(such as a college or railway network). A person in emrergency can post his condition which will be brought in notice to the one observing the network so that he can take actions accordingly. This promotes government to maintain a emergency network which will be useful when a ban is applied. The people may not be able to communicate to each other but emergency reporting can be done reliably. This feature will be accompanied with a post verifier which verifies the reliability of a post in later editions. Here the concerned user can add details and also attach an image of the incident.

  
## Built with

* [Firebase](https://firebase.google.com/) - Used for a large number of features.
* [Maven](https://dl.google.com/dl/android/maven2/index.html) - Dependency Management.
* [Chirp](https://chirp.io/) - AudioQR technology.
* [Google-NearbyConnections](https://developers.google.com/nearby/connections/overview) - For connecting to nearby android devices.
* [Laravel](https://laravel.com/) - For Backend

## Contributing

I am open to contributions. Contact me on [Facebook](https://www.facebook.com/mishraprateekaries) for any queries.

See also the list of [contributors](https://github.com/thesmallstar/Plexus/contributors) who participated in the project.

## Acknowledgements
* We are very thankful to Medicaps for conducting Code-tussel Aarmbh and giving us such marvellous opportunities to build upon our existing skills and showcase our talent.
