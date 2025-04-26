defmodule VeraWeb.Router do
  use VeraWeb, :router

  pipeline :api do
    plug :accepts, ["json"]
  end

  scope "/api", VeraWeb do
    pipe_through :api
  end
end
