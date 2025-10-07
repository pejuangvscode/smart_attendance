```mermaid
graph TD
    A[Start] --> B{User Logged In?}
    B -->|No| C[Login]
    C --> D{Valid Credentials?}
    D -->|No| C
    D -->|Yes| E[Home Screen]
    B -->|Yes| E[Home Screen]
    E --> F{Select Activity}
    F --> G[Melakukan Presensi]
    F --> H[View Statistic Attendence] 
    F --> I[View Weekly Status]
    F --> J[View Schedule]
    F --> K[View History Kehadiran]
    F --> L[Request Izin atau Sakit]
    F --> M[Log Out]
    G --> N[Take Photo]
    N --> O[Submit Attendance]
    O --> P[Attendance Complete]
    P --> E
    H --> Q[Display Statistics]
    Q --> E
    I --> R[Display Weekly Status]
    R --> E
    J --> S[Display Schedule]
    S --> T{Submit from Schedule?}
    T -->|Yes| N
    T -->|No| E
    K --> U[Display Attendance History]
    U --> E
    L --> V[Fill Permission Request]
    V --> W[Submit Request]
    W --> X[Request Submitted]
    X --> E
    M --> Y[Logout Confirmation]
    Y --> Z[End]
    P --> AA{Continue?}
    AA -->|Yes| E
    AA -->|No| Z
    Q --> AA
    R --> AA
    S --> AA
    U --> AA
    X --> AA
