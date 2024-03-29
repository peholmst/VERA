// Code generated by protoc-gen-go-grpc. DO NOT EDIT.

package proto

import (
	context "context"
	grpc "google.golang.org/grpc"
	codes "google.golang.org/grpc/codes"
	status "google.golang.org/grpc/status"
)

// This is a compile-time assertion to ensure that this generated file
// is compatible with the grpc package it is being compiled against.
// Requires gRPC-Go v1.32.0 or later.
const _ = grpc.SupportPackageIsVersion7

// AlertServiceClient is the client API for AlertService service.
//
// For semantics around ctx use and closing/ending streaming RPCs, please refer to https://pkg.go.dev/google.golang.org/grpc/?tab=doc#ClientConn.NewStream.
type AlertServiceClient interface {
	SubscribeToAlerts(ctx context.Context, in *AlertSubscriptionRequest, opts ...grpc.CallOption) (AlertService_SubscribeToAlertsClient, error)
	AcknowledgeAlert(ctx context.Context, in *AlertAcknowledgementRequest, opts ...grpc.CallOption) (*AlertAcknowledgementResponse, error)
}

type alertServiceClient struct {
	cc grpc.ClientConnInterface
}

func NewAlertServiceClient(cc grpc.ClientConnInterface) AlertServiceClient {
	return &alertServiceClient{cc}
}

func (c *alertServiceClient) SubscribeToAlerts(ctx context.Context, in *AlertSubscriptionRequest, opts ...grpc.CallOption) (AlertService_SubscribeToAlertsClient, error) {
	stream, err := c.cc.NewStream(ctx, &AlertService_ServiceDesc.Streams[0], "/vera.alert.proto.AlertService/SubscribeToAlerts", opts...)
	if err != nil {
		return nil, err
	}
	x := &alertServiceSubscribeToAlertsClient{stream}
	if err := x.ClientStream.SendMsg(in); err != nil {
		return nil, err
	}
	if err := x.ClientStream.CloseSend(); err != nil {
		return nil, err
	}
	return x, nil
}

type AlertService_SubscribeToAlertsClient interface {
	Recv() (*Alert, error)
	grpc.ClientStream
}

type alertServiceSubscribeToAlertsClient struct {
	grpc.ClientStream
}

func (x *alertServiceSubscribeToAlertsClient) Recv() (*Alert, error) {
	m := new(Alert)
	if err := x.ClientStream.RecvMsg(m); err != nil {
		return nil, err
	}
	return m, nil
}

func (c *alertServiceClient) AcknowledgeAlert(ctx context.Context, in *AlertAcknowledgementRequest, opts ...grpc.CallOption) (*AlertAcknowledgementResponse, error) {
	out := new(AlertAcknowledgementResponse)
	err := c.cc.Invoke(ctx, "/vera.alert.proto.AlertService/AcknowledgeAlert", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

// AlertServiceServer is the server API for AlertService service.
// All implementations must embed UnimplementedAlertServiceServer
// for forward compatibility
type AlertServiceServer interface {
	SubscribeToAlerts(*AlertSubscriptionRequest, AlertService_SubscribeToAlertsServer) error
	AcknowledgeAlert(context.Context, *AlertAcknowledgementRequest) (*AlertAcknowledgementResponse, error)
	mustEmbedUnimplementedAlertServiceServer()
}

// UnimplementedAlertServiceServer must be embedded to have forward compatible implementations.
type UnimplementedAlertServiceServer struct {
}

func (UnimplementedAlertServiceServer) SubscribeToAlerts(*AlertSubscriptionRequest, AlertService_SubscribeToAlertsServer) error {
	return status.Errorf(codes.Unimplemented, "method SubscribeToAlerts not implemented")
}
func (UnimplementedAlertServiceServer) AcknowledgeAlert(context.Context, *AlertAcknowledgementRequest) (*AlertAcknowledgementResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method AcknowledgeAlert not implemented")
}
func (UnimplementedAlertServiceServer) mustEmbedUnimplementedAlertServiceServer() {}

// UnsafeAlertServiceServer may be embedded to opt out of forward compatibility for this service.
// Use of this interface is not recommended, as added methods to AlertServiceServer will
// result in compilation errors.
type UnsafeAlertServiceServer interface {
	mustEmbedUnimplementedAlertServiceServer()
}

func RegisterAlertServiceServer(s grpc.ServiceRegistrar, srv AlertServiceServer) {
	s.RegisterService(&AlertService_ServiceDesc, srv)
}

func _AlertService_SubscribeToAlerts_Handler(srv interface{}, stream grpc.ServerStream) error {
	m := new(AlertSubscriptionRequest)
	if err := stream.RecvMsg(m); err != nil {
		return err
	}
	return srv.(AlertServiceServer).SubscribeToAlerts(m, &alertServiceSubscribeToAlertsServer{stream})
}

type AlertService_SubscribeToAlertsServer interface {
	Send(*Alert) error
	grpc.ServerStream
}

type alertServiceSubscribeToAlertsServer struct {
	grpc.ServerStream
}

func (x *alertServiceSubscribeToAlertsServer) Send(m *Alert) error {
	return x.ServerStream.SendMsg(m)
}

func _AlertService_AcknowledgeAlert_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(AlertAcknowledgementRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(AlertServiceServer).AcknowledgeAlert(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/vera.alert.proto.AlertService/AcknowledgeAlert",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(AlertServiceServer).AcknowledgeAlert(ctx, req.(*AlertAcknowledgementRequest))
	}
	return interceptor(ctx, in, info, handler)
}

// AlertService_ServiceDesc is the grpc.ServiceDesc for AlertService service.
// It's only intended for direct use with grpc.RegisterService,
// and not to be introspected or modified (even as a copy)
var AlertService_ServiceDesc = grpc.ServiceDesc{
	ServiceName: "vera.alert.proto.AlertService",
	HandlerType: (*AlertServiceServer)(nil),
	Methods: []grpc.MethodDesc{
		{
			MethodName: "AcknowledgeAlert",
			Handler:    _AlertService_AcknowledgeAlert_Handler,
		},
	},
	Streams: []grpc.StreamDesc{
		{
			StreamName:    "SubscribeToAlerts",
			Handler:       _AlertService_SubscribeToAlerts_Handler,
			ServerStreams: true,
		},
	},
	Metadata: "alert_api_gateway.proto",
}
