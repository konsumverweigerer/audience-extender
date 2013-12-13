define(["knockout", "lib/models", "jsRoutes"], (ko,mod) ->
  loadwebsites = (mod,models) ->

  loadaudiences = (mod,models) ->

  loadpackages = (mod,models) ->

  loadcampaigns = (mod,models) ->

  loadaudience = (mod,models) ->

  loadcampaign = (mod,models) ->

  loadadmins = (mod,models) ->

  loadpublishers = (mod,models) ->

  { generate: (mod,models,page) ->
    if 'audience'==page
      loadwebsites(mod,models)
      loadaudiences(mod,models)
      loadaudience(mod,models)
    else if 'campaign'==page
      loadpackages(mod,models)
      loadaudiences(mod,models)
      loadcampaigns(mod,models)
      loadcampaign(mod,models)
    else if 'admin'==page
      loadadmins(mod,models)
    else if 'publisher'==page
      loadpublishers(mod,models)
  }
)