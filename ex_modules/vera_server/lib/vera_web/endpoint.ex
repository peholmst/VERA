defmodule VeraWeb.Endpoint do
  use Phoenix.Endpoint, otp_app: :vera

  # Generate a unique ID for each request
  plug Plug.RequestId

  # Parse the request body with a known parser if available.
  plug Plug.Parsers,
    parsers: [:urlencoded, :multipart, :json],
    pass: ["*/*"],
    json_decoder: Phoenix.json_library()

  # Send request to router
  plug VeraWeb.Router
end
