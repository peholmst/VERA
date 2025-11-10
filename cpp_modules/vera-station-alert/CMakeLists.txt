cmake_minimum_required(VERSION 3.28)
project(VeraStationAlert)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)

add_subdirectory(vendored/SDL)

set(SDL3_FOUND TRUE)
set(SDL3_INCLUDE_DIRS "${CMAKE_SOURCE_DIR}/vendored/SDL/include")
set(SDL3_LIBRARIES SDL3::SDL3)

add_subdirectory(vendored/SDL3_gfx)

add_executable(VeraStationAlert 
    src/main.cpp
    src/AnalogClock.cpp
)

target_link_libraries(VeraStationAlert PRIVATE 
    SDL3::SDL3 
    SDL3_gfx_Static
)

target_include_directories(VeraStationAlert PRIVATE
    "${CMAKE_SOURCE_DIR}/vendored/SDL/include"
    "${CMAKE_SOURCE_DIR}/vendored/SDL3_gfx"
)