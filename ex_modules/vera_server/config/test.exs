# Test environment configuration
import Config

config :vera, VeraWeb.Endpoint,
  server: false

# Print only warnings and errors during test
config :logger, level: :warning

# Initialize plugs at runtime for faster test compilation
config :phoenix, :plug_init_mode, :runtime
