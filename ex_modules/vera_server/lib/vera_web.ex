defmodule VeraWeb do
  @moduledoc """
  TODO Document me
  """

  # TODO Explain what this is
  def router do
    quote do
      use Phoenix.Router, helpers: false

      import Plug.Conn
      import Phoenix.Controller
    end
  end

  @doc """
  When used, dispatch to the appropriate controller/live_view/etc.
  """
  defmacro __using__(which) when is_atom(which) do
    # TODO Explain what this does
    apply(__MODULE__, which, [])
  end
end
