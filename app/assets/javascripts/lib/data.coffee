define(["knockout", "lib/models", "jsRoutes"], (ko,mod) ->
  loadwebsites = (mod,models) ->

  loadaudiences = (mod,models) ->

  loadpackages = (mod,models) ->

  loadcampaigns = (mod,models) ->

  loadaudience = (mod,models) ->
    models.audiencechartdaterange.dataloader = ->
      st = models.audiencechartdaterange.startDate().getTime()
      et = models.audiencechartdaterange.endDate().getTime()
      r = routes.controllers.AudienceController.dashboard(st,et)
      r.ajax {
       success: (d) ->
         models.audiencechart.chartcontent d
      }
    models.audiencechartdaterange.dataloader()

  loadcampaign = (mod,models) ->
    models.campaignchartdaterange.dataloader = ->
      st = models.campaignchartdaterange.startDate().getTime()
      et = models.campaignchartdaterange.endDate().getTime()
      r = routes.controllers.CampaignController.dashboard(st,et)
      r.ajax {
       success: (d) ->
         models.campaignchart.chartcontent d
      }
    models.campaignchartdaterange.dataloader()

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