# AlarmNest——Smart Nested Alarms for Easier Time Management

Do you often struggle to wake up on time or feel overwhelmed by scattered reminders throughout the day? What if your alarm clock could 
do more than just wake you up—what if it could help you plan your day, stay organized, and even motivate you? This project reimagines the 
alarm clock as a smarter, friendlier assistant. With features like nested alarms, fun wake-up challenges, location-based reminders, and 
personalized suggestions, AlarmNest is designed to make time management easier and mornings more enjoyable.

This project is a course project of CUHK CSCI3310 - Mobile Computing & Application Development (2024-2025 R2), done by Group 20. 

## Build Instructions

**Version of Android Studio:** Android Studio Meerkat | 2024.3.1 Patch 1

1. Ensure the project includes `secrets.properties` and `local.defaults.properties` files, and that `MAPS_API_KEY` is correctly configured. Without these keys, Google Maps and Google Places functionalities may not work properly.

2. The testing device used during development is **Medium Phone**, running **Android 16.0 ("Baklava")** on arm64 architecture, with support for Google Play services.

3. Open the project in Android Studio, wait for the Gradle synchronization to complete, and then build the project via Android Studio.

4. Finally, run the application on an emulator or a physical device that meets the specifications above.

## Permission Requirements

AlarmNest requires the following Android permissions, declared in the manifest:
  - `SCHEDULE_EXACT_ALARM` for Precise alarm scheduling
  - `POST_NOTIFICATIONS` for Display notifications
  - `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE` for Read/Write alarm backup data
  - `MANAGE_EXTERNAL_STORAGE` for Full storage access
  - `ACTIVITY_RECOGNITION` for Step detection sensor access

The AlarmPermission object handles runtime permission requests with user-friendly dialogs. The permission dialog is displayed when storage permissions are denied.

## Layout Flow

<img width="1587" height="2245" alt="提取自AlarmNest - CSCI3310 Project Phase II Report" src="https://github.com/user-attachments/assets/3e6c796a-4be5-400d-a02b-bc06bd210a98" />

### A. Permission Management

**Only when AlarmNest is launched for the first time on a device**, a series of dialogs will guide the user through the necessary authorization steps. 

Users will be prompted to enable four essential permissions: `Exact Alarm`, `Activity Recognition`, `Notification`, and `Storage`. After enabling these, Alarmnest needs to be restarted for the permissions to take effect. Subsequent launches of AlarmNest will not display the same permission request dialogs, allowing users to set alarms normally.

### B. Clock Setting

AlarmNest starts with a `ClockListScreen`, which displays a series of nested alarms integrated in the form of main alarms and sub-alarms. By clicking different components of the alarm card, a dialog will pop up to **allow detailed settings**, such as changing the time, adding descriptions, setting repeat modes, specifying triggering methods, and more.

The floating add button located at the bottom right corner allows users to **add either a `time alarm` or a `location alarm`**. When selecting to add a `time alarm`, a time picker dialog appears, where users can pick a time and add a new alarm. When selecting to add a `location alarm`, the app navigates to a new screen where users can set the location and trigger radius. The newly added time or location alarm will be displayed in the `ClockListScreen`.

### C. Alarm Triggering

When the alarm's time or location meets the triggering conditions, **alarm music** will play, and **a series of notifications** will be sent to prompt the user to return to the `AlarmNest` app to turn off the alarm. It is worth noting that **even if the app is closed, the alarm will still ring when triggered**.

By clicking the notification or opening the app, users will be directed to the `Alarm Triggering Screen`. Users will navigate to the corresponding alarm mode that has been set, such as a walking alarm requiring the user to walk 20 steps or a typing alarm requiring the user to correctly type a specific sentence. **The alarm sound will not stop until the task is completed and the button is pressed**.

After completing the alarm task, users will be directed to the `Alarm Off Screen`. This screen will close and return to the Clock List Screen either after the button is clicked or after waiting for 3 seconds.


## Top 3 Most Interesting/Technically Challenging Features

### A. Main Alarm/Sub-Alarm Architecture

This feature provides a unique approach to time management by offering a main alarm/sub alarm architecture based on **time differences**. It can dynamically reset subalarms based on main alarms' change, optimizing time management efficiency. 

The technical challenges involve designing an architecture that can dynamically adjust alarms based on event dependencies, ensuring seamless integration and accurate time management. The design of the database structure, correct and timely synchronization between the frontend and backend, and data preservation and recovery when the app is interrupted or exited are all issues that need careful consideration. This approach enhances the app's functionality by providing users with efficient and reliable alarm settings tailored to their schedules.

### B. Triggering Alarms Outside the App

As a core functionality of an alarm app, triggering alarm sounds is both essential and technically challenging. AlarmNest utilizes the system's `AlarmManager` to set precise alarms and employs **`PendingIntent`** for inter-process communication, sending alarm-trigger events to `AlarmReceiver`. Note that whenever the phone restarts, the `PendingIntent` is cleared, so you need to reopen AlarmNest to restore the alarms. To prevent the system from sleeping, a **`WakeLock`** is used to wake up the CPU, while `AlarmService` operates as a foreground service to play alarm sounds.

This feature involves the integration of two Android component types—services and broadcast receivers—along with the need to efficiently schedule multi-threaded tasks, making its technical implementation highly complex.

### C. LLM Integration

Applying large language models to traditional applications to improve efficiency is an interesting topic. Here, I integrate **the `qwen2.5-72b` huggingface model** into the alarm app to generate personalized sub-alarm recommendations based on user habits, optimizing alarm times and note settings.

The technical challenges of this feature lie in designing suitable data structures and algorithms in prompts, as well as processing historical data to generate personalized suggestions. Transforming user behavior data into actionable recommendations enhances the app's intelligence and provides a more convenient alarm-setting experience through cutting-edge LLM technology.

## Features Brief Description

### A. From the Perspective of Components Usage

1. AlarmNest is built using **single-Activity mode** and `Jetpack Compose` for UI construction, **simplifying lifecycle management**. Additionally, error handling and recovery mechanisms are provided wherever possible to enhance lifecycle management reliability.
2. Internally, AlarmNest primarily uses `Navigation Receiver` to switch pages in a router-like manner across different screens. External requests, such as opening the settings panel, are handled using intents.
3. A `PermissionManager` class has been created to unify permission management, including permissions for precise alarms, activity recognition, notifications, and storage. This ensures a **user-friendly permission request process**.
4. The system's `AlarmReceiver` is used to set precise alarm times, with `PendingIntent` sending alarm-triggering events to the `AlarmReceiver`. Additionally, `AlarmService` is used as a foreground service to acquire a `WakeLock` to prevent sleep, play alarm music, and send notifications. **This ensures that even if the app is killed, the system's `AlarmManager` can still trigger alarms**.
5. The app uses `Room Database` to establish a robust database and entity definitions, providing **convenient CRUD operations**. The WAL mode is enabled, and periodic database maintenance is performed for data cleanup, **optimizing performance**. KSP is utilized to make database operations more efficient and reliable.
6. `Google Maps API` and `Google Places API` are integrated to enable **map-based location tracking and location retrieval** functionality. Meanwhile, `Google Play Service`s' `Geofencing API` is utilized to implement location-based alarm triggering through **geofencing**.
7. For the walking alarm feature, the app can use `Android's Sensor API` to detect steps by listening to either the **`TYPE_STEP_DETECTOR`** sensor or the **`TYPE_ACCELEROMETER`** sensor.

8. Integration of `qwen2.5` network requests to optimize automatic time and note settings for alarms **using LLMs**.

9. Use of the system's **`MediaStore Content Provider`** to upload audio files in suitable formats and sizes for setting custom alarm sounds.
10. By using **`WorkManager`** to automatically execute tasks on background threads, the framework's intelligent scheduling of background tasks achieves the goal of **Battery Optimization**.

### B. From the Perspective of Unique Features

1. Provides a simple **main alarm/sub-alarm architecture** based on "event time differences" to dynamically set alarms, optimizing time management efficiency. By leveraging event associations, it ensures no details are missed.

2. Integrates Google Maps to offer location-based alarm triggers, providing **dynamic reminders** for events with uncertain timing.

   > Although the geofencing feature is implemented in the app, in reality, the location alarm has a high risk of failing or not triggering in time. Therefore, location-based alarms are disabled by default.

3. Implements more complex and **harder-to-dismiss Wake-up Alarms,** requiring users to focus during responses, helping them prepare for upcoming tasks in better condition.

4. Integrates large language models to generate **personalized reminder suggestions** based on user habits and historical data. This offers lightweight intelligent Alarm Recommendations, reducing the time spent on manual input and alarm setup.

5. Supports custom ringtone selection and integrates the system's media library, enabling more personalized alarm settings. Multiple wake-up verification methods can be configured, allowing users to **tailor reminder strategies to different scenarios**.

## Contributors

CHEN DANGGUI - Under the guidance of the instructor, all the aforementioned functionalities of this project, as well as the documentation, were completed independently by me as a one-person group (Group 20).
