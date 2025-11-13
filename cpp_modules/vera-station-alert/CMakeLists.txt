cmake_minimum_required(VERSION 3.28)
project(VeraStationAlert)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)

add_subdirectory(vendored/SDL)
add_subdirectory(vendored/SDL_ttf)

add_executable(VeraStationAlert 
    src/main.cpp
    src/Alert.cpp
    src/AlertScreen.cpp
    src/AnalogClock.cpp
)

target_link_libraries(VeraStationAlert PRIVATE 
    SDL3::SDL3 
    SDL3_ttf-static
)

target_include_directories(VeraStationAlert PRIVATE
    "${CMAKE_SOURCE_DIR}/vendored/SDL/include"
    "${CMAKE_SOURCE_DIR}/vendored/SDL_ttf/include"
)
