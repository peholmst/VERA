find_package(protobuf CONFIG REQUIRED)
find_package(gRPC CONFIG REQUIRED)
find_package(Threads)

set(PROTO_FILES
    src/vera/alert/proto/alert_api_gateway.proto
)

add_library(alert_api_gateway_proto ${PROTO_FILES})
target_link_libraries(alert_api_gateway_proto
    PUBLIC
        protobuf::libprotobuf
        gRPC::grpc
        gRPC::grpc++
)
target_include_directories(alert_api_gateway_proto PUBLIC ${CMAKE_CURRENT_BINARY_DIR})

set(PROTO_BINARY_DIR "${CMAKE_CURRENT_BINARY_DIR}")
set(PROTO_IMPORT_DIRS "${CMAKE_CURRENT_LIST_DIR}/src")

get_target_property(grpc_cpp_plugin_location gRPC::grpc_cpp_plugin LOCATION)

protobuf_generate(
    TARGET alert_api_gateway_proto 
    LANGUAGE cpp 
    OUT_VAR PROTO_GENERATED_FILES 
    IMPORT_DIRS ${PROTO_IMPORT_DIRS} 
    PROTOC_OUT_DIR "${PROTO_BINARY_DIR}"
)
set_source_files_properties(${PROTO_GENERATED_FILES} PROPERTIES SKIP_UNITY_BUILD_INCLUSION on)

protobuf_generate(
    TARGET alert_api_gateway_proto 
    LANGUAGE grpc 
    OUT_VAR PROTO_GENERATED_FILES 
    GENERATE_EXTENSIONS .grpc.pb.h .grpc.pb.cc 
    PLUGIN "protoc-gen-grpc=${grpc_cpp_plugin_location}"
    IMPORT_DIRS ${PROTO_IMPORT_DIRS} 
    PROTOC_OUT_DIR "${PROTO_BINARY_DIR}"
)
set_source_files_properties(${PROTO_GENERATED_FILES} PROPERTIES SKIP_UNITY_BUILD_INCLUSION on)
