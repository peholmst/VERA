cmake_minimum_required(VERSION 3.28)
project(VeraStationAlert)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)

add_subdirectory(vendored/SDL)
add_subdirectory(vendored/SDL_ttf)

function(create_resources dir output namespace)
    file(WRITE ${output} "// Auto-generated file.\n\n#include \"Fonts.hpp\"\n")
    message(STATUS "Generating ${output}")
    file(GLOB bins ${dir}/*)
    foreach(bin ${bins})
        string(REGEX MATCH "([^/]+)$" filename ${bin})
        string(MAKE_C_IDENTIFIER ${filename} variable_name)
        file(READ ${bin} content HEX)
        
        string(REGEX REPLACE "([0-9a-f][0-9a-f])" "0x\\1, " content "${content}")

        set(array_definition "unsigned char ${namespace}::${variable_name}[] =\n{\n${content}\n};")
        set(size_definition "size_t ${namespace}::${variable_name}_size = sizeof(${namespace}::${variable_name});")
        set(source "${array_definition}\n${size_definition}\n")

        message(STATUS "Appending ${variable_name} to ${output}")
        file(APPEND "${output}" "${source}")
    endforeach()
endfunction()

create_resources("${CMAKE_SOURCE_DIR}/fonts" src/GeneratedFontsData.cpp Fonts)

add_executable(VeraStationAlert 
    src/main.cpp
    src/Alert.cpp
    src/AlertScreen.cpp
    src/AnalogClock.cpp
    src/GeneratedFontsData.cpp
)

target_link_libraries(VeraStationAlert PRIVATE 
    SDL3::SDL3 
    SDL3_ttf-static
)

target_include_directories(VeraStationAlert PRIVATE
    "${CMAKE_SOURCE_DIR}/vendored/SDL/include"
    "${CMAKE_SOURCE_DIR}/vendored/SDL_ttf/include"
)
