find_package(Threads)

set(SOURCES
    src/main.cc
)

source_group(TREE ${CMAKE_CURRENT_SOURCE_DIR} FILES ${SOURCES})

add_executable(alert_api_gateway_server ${SOURCES})
target_link_libraries(alert_api_gateway_server
    PRIVATE
        alert_api_gateway_proto
)
