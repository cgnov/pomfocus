# Pomodoro Focus

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Pomodoro Focus combines the pomodoro method and gamification to make productivity easy. Automated timers and push notifications keep you on track, and you can compete with yourself or with friends to accomplish your goals.

### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Productivity
- **Mobile:** Push notifications are essential for this app. In addition, the mobile format allows for better aiding in non-computer-based productivity.
- **Story:** This app makes productivity bite-sized and attractive with streaks, achievements, and competition among friends.
- **Market:** The wide array of productivity apps that currently exist demonstrate the strong interest in carving out time and making the best of the addictive nature of phones. This app would be particularly strong with students, freelancers, and anyone that wants to productively use their unstructured time.
- **Habit:** People work on their tasks every day. With encouraging and guiding notifications, the app would mostly work in the background but still be present. The goal is not to have the app open for lengthy periods of time but to create a positive experience that guides users to start their timers.
- **Scope:** The core of the app is alternating timers, which is useful for pomodoro method enthusiasts but isn't particularly attractive. The next main component would be keeping track of one's own completion, such as with streaks, achievements, and/or visual representations. The third is comparison between users, such as with leaderboards. Having the MVP of each component would make an interesting and useful app, and fleshing each block out would serve to make the app more attractive and satisfying to use.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* Allow signup
* Allow logging in and out
* Allow user to start 25- and 5-minute timers and be notified through push notification upon completion
* Store user's 25-minute timer completion
* Allow completion comparison with at least one other person, such as with leaderboards

**Optional Nice-to-have Stories**

* Add friends
    * Allow user to compare completion rate in friends-only leaderboard
    * Search users to add friend
    * Ask to be notified when friend is working (push notification that says: "Jane started focusing, join in!")
* Profile view
    * Display name/handle, photo, streaks/achievements if relevant, and number of pomodoros completed
* Focus time
    * Allow user to set days and times that they work (won't get notifications from the app outside of those days/times)
* Streaks/Achievements
    * Notify user 1 hour before end of their day to maintain streak
    * Milestone achievements
        * 3 days in a row, 7 days in a row
        * 4 pomodoros in a day, 8, 12, 16
        * Restarted streak achievement (discourage missing two days in a row)
        * 30 days since first pomodoro (push notif reminder to attract users who still have app downloaded but haven't been using recently)
    * Leaderboard achievements
        * Get to top 5, etc
* Break recommendations (drink some water, walk around a bit)
* Task-based reminders
    * Allow user to put in tasks that are attainable in half an hour (ask what they want to work on, then what they can get done in less than half an hour)
    * When sending reminder for beginning of day, say something like "Start out your day by ____" (ex: "Start out your day by clearing your floor of food and clothing")
    * Allow user to put tasks into categories and only be reminded of those categories on certain days/times (weekdays could be Work, weekends could be Leisure)
* Allow user to enable calming focus music during 25-minute timer
* Focus mode (break timer when close app, send warning notification)


### 2. Screen Archetypes

* Login/Register
    * Allow user to sign up
    * Allow user to log in
* Creation - Timer
    * Allow user to start 25- and 5-minute timers
* Stream - Leaderboard
    * Display top few users by pomodoro completion, display current user's ranking
    * (Optional) Display top few friends by pomodoro completion, display current user's ranking
* Detail - Profile (optional)
    * Display streak, 7-day completion, all-time completion
    * Display profile photo (allow taking profile photo with camera)
    * Display achievements (if own profile, can also show achievements not yet achieved)

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Timer - Home Page
* Leaderboard
    * All
    * Friends
* Profile

**Flow Navigation** (Screen to Screen)

* Login/Signup
    * Timer/Home
* Timer/Home
    * => None
* Leaderboard
    * => Profile of users (optional)
* Profile
    * => Friends list (stream) (optional)
        * =>Profile of users
    * => History (stream) (optional)

## Wireframe
<img src="PomFocus Wireframe.jpg" width=600>

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 

### Models
#### Focus Time
| Name       | Type            | Description                                                                     |
| ---------- | --------------- | ------------------------------------------------------------------------------- |
| objectId   | String          | required, unique id for focus session (default field)                           |
| creator    | Pointer to User | required, user that completed said focus session                                |
| createdAt  | DateTime        | required, date and time of when user completed session (default field)          |
| length     | Number          | length of focus session in minutes (useful for implementing non-25-min options) |
| category   | String          | User-selected category describing session                                       |

#### User
| Name | Type | Description |
| ---- | ---- | ----------- |
| objectId | String | required, unique id for focus session (default field) |
| username | String | required, user's handle (unique to user) (default field) |
| firstName | String | required, user's first name |
| password | String | required, self explanatory (default field) |
| avatar | File | image avatar for user |
| streak | Number | length of current streak |
| lastFocus | DateTime | date and time of user's last session (used to calc streak) |
| friends | Relation | users that this user is friends with |
| friendRequests | Relation | users that have requested to be friends with this user and have not yet been accepted |
| totalFocus | Number | time in minutes spent focusing in total |
| categories | Relation | categories that this user has created |

#### Category (optional)
| Name | Type | Description |
| ---- | ---- | ---- |
| objectId | String | required, unique id for category (default field) |
| creator | Pointer to User | required, user that created this category |
| name | String | required, name describing category |
| totalFocus | Number | time in minutes spent focusing in this category |
| focuses | Relation | focus sessions related to this category |
| streak | Number | length of current streak in this category |
| lastFocus | DateTime | date and time of user's last focus session in this category |

### Networking
* Timer screen
   * (Create/POST) Create new Focus object (will likely happen in background of app)
* Leaderboard
   * (Read/GET) Query top focusers
   ```
   final ParseQuery<User> query = ParseQuery.getQuery(User.class);
   query.addDescendingOrder(User.KEY_FOCUS);
   query.setLimit(10);
   query.findInBackground(new FindCallback<Post>() {
      @Override
      public void done(List<User> users, ParseException e) {
          if (e != null) {
              Log.e(TAG, "Issue with getting top focusers", e);
          } else {
              // Users have been successfully queried
              adapter.addAll(users);
          }
      }
   });
   ```
   * (Read/GET) Query focus summary info where current user is creator if current user isn't top focuser
* Personal profile screen
   * (Read/GET) Query logged in user object (show handle, name, streak, achievements, friends, etc.)
   * (Update/PUT) Update user profile image
   * (Update/PUT) Accept pending friend requests, create friendship
   * (Read/GET) Query users matching user-inputted handle string, send to profile screen
   * (Read/GET) Query focus information where current user is creator by category (would need Category model)
   ```
   final ParseQuery<Category> query = ParseQuery.getQuery(Category.class);
      query.equalTo(Category.KEY_USER, ParseUser.getCurrentUser());
      query.findInBackground(new FindCallback<Focus>() {
         @Override
         public void done(List<Category> categories, ParseException e) {
             if (e != null) {
                 Log.e(TAG, "Issue with getting user's categories", e);
             } else {
                 // Categories have been successfully queried
                 adapter.addAll(categories);
             }
         }
      });
   ```
* Other profile screen
   * (Read/GET) Query relevant user object
   * (Read/GET) Query focus summary info (number of focuses) where this user is creator
   * (Update/PUT) Send friend request
