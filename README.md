# Questa: Multi-Question Poll App

## Overview

Questa is a mobile app for creating and participating in multi-question polls. Users can create polls with multiple questions, each with customizable options, and categorize them for easy discovery. Other users can vote on these polls and view the results presented through intuitive visualizations.

## Features

### User Authentication
- Register with email, password, and username
- Login with email and password
- Password reset functionality
- Profile management (edit username)

### Poll Management
- Create multi-question polls with customizable options
- Categorize polls by topic (Technology, Sports, Politics, etc.)
- Vote on polls with a simple, intuitive interface
- Progress through multi-question polls sequentially
- View detailed results with pie charts and statistics

### Social Features
- Add favorite polls for quick access
- Comment on polls to discuss results with others
- View polls created by specific users

### Browsing Experience
- View all polls in a feed
- Filter polls by category with horizontal scrolling
- Tab navigation between All Polls, Featured, Favorites, and My Polls
- Search polls by keywords

## Technical Architecture

### Backend
- **Firebase Authentication**: Manages user authentication and profile data
- **Firebase Realtime Database**: Stores poll data, votes, comments, and user preferences

### Frontend
- **Jetpack Compose**: Modern UI toolkit for building native Android UI
- **MVVM Architecture**: Separation of UI, business logic, and data
- **Koin**: Dependency injection framework
- **Kotlin Coroutines & Flow**: Asynchronous programming and reactive streams

## Data Structure

### Polls
```json
{
  "polls": {
    "poll_id_1": {
      "id": "poll_id_1",
      "userId": "user123",
      "creatorName": "JohnDoe",
      "questions": [
        {
          "id": "q1",
          "question": "What is your favorite programming language?",
          "options": [
            {
              "id": "opt1",
              "text": "Kotlin",
              "votes": 15,
              "percentage": 75.0
            },
            {
              "id": "opt2",
              "text": "Java",
              "votes": 5,
              "percentage": 25.0
            }
          ]
        },
        {
          "id": "q2",
          "question": "What's your preferred IDE?",
          "options": [
            {
              "id": "opt3",
              "text": "Android Studio",
              "votes": 12,
              "percentage": 60.0
            },
            {
              "id": "opt4",
              "text": "IntelliJ IDEA",
              "votes": 8,
              "percentage": 40.0
            }
          ]
        }
      ],
      "totalVotes": 20,
      "createdAt": 1623456789000,
      "category": "Technology",
      "isFeatured": true,
      "likes": ["user456", "user789"]
    }
  }
}
```

### Users
```json
{
  "users": {
    "user123": {
      "uid": "user123",
      "email": "john@example.com",
      "username": "JohnDoe",
      "bio": "Poll enthusiast",
      "favoritePolls": ["poll_id_1", "poll_id_2"],
      "votedPolls": ["poll_id_1"]
    }
  }
}
```

### Comments
```json
{
  "comments": {
    "comment_id_1": {
      "id": "comment_id_1",
      "pollId": "poll_id_1",
      "userId": "user456",
      "username": "JaneDoe",
      "text": "Interesting results!",
      "createdAt": 1623456999000
    }
  }
}
```

## Architecture Components

### ViewModels
- **AuthViewModel**: Manages authentication state and user operations
- **PollViewModel**: Handles poll creation, voting, and retrieval
- **CommentViewModel**: Manages comments on polls
- **ProfileViewModel**: Handles user profile updates

### Repositories
- **AuthRepository**: Interface for Firebase Authentication
- **PollRepository**: Manages poll data in Firebase Realtime Database
- **CommentRepository**: Handles comment operations
- **UserRepository**: Manages user profile data

### UI Screens
- **LoginScreen & RegisterScreen**: Authentication interfaces
- **HomeScreen**: Main feed with poll listing and filtering
- **CreatePollScreen**: Interface for creating multi-question polls
- **PollDetailScreen**: Display and interaction with poll questions and results
- **ProfileScreen**: User profile management
- **EditProfileScreen**: Interface for updating user information

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or newer
- JDK 11 or newer
- Firebase account for backend services

### Setup
1. Clone the repository
2. Set up Firebase project with Authentication and Realtime Database
3. Add the Firebase configuration file (google-services.json) to the app directory
4. Build and run the application

## Best Practices

### Creating Effective Polls
- Keep questions clear and concise
- Provide distinct and non-overlapping answer options
- Use appropriate categories for better discovery
- Include at least 2-4 options per question for meaningful results

### Using the App
- Browse categories to find relevant polls
- Save interesting polls to favorites for later access
- Comment to engage with poll creators and other voters
- Create polls on topics you're curious about to gain insights

## License
This project is licensed under the MIT License.
