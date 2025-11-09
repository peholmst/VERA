cmake_minimum_required(VERSION 3.28)
project(VeraStationAlert)

set(CMAKE_CXX_STANDARD 23)

add_subdirectory(src)
add_subdirectory(vendored/SDL EXCLUDE_FROM_ALL)
