define(["knockout", "lib/models", "jsRoutes", "jquery"], (ko,mod) ->
  tojson = (d) ->
    if d instanceof String || typeof(d)=='string'
      return $.parseJSON d
    return d

  loadwebsites = (mod,models) ->
    websites = (nv) ->
      if nv?.id?
        id = nv.id()
        r = routes.controllers.AudienceController.websiteList id
        r.ajax {
          success: (d) ->
            models.websites []
            models.websites.push new mod.Website x for x in tojson d
            v.refresh models.websites() for v in models.audiences()
            models.audiencetablesearchbar?.search()
        }
    models.publisher.subscribe = (nv) -> websites nv
    websites models.publisher()

  loadaudiences = (mod,models) ->
    audiences = (nv) ->
      if nv?.id?
        id = nv.id()
        r = routes.controllers.AudienceController.audienceList id
        r.ajax {
          success: (d) ->
            models.audiences []
            models.audiences.push new mod.Audience x for x in tojson d
            if models.websites?
              v.refresh models.websites() for v in models.audiences()
            models.audiencetablesearchbar?.search()
        }
    models.publisher.subscribe = (nv) -> audiences nv
    audiences models.publisher()

  loadpackages = (mod,models) ->
    packages = (nv) ->
      if nv?.id?
        id = nv.id()
        r = routes.controllers.CampaignController.packageList id
        r.ajax {
          success: (d) ->
            models.packages []
            models.packages.push new mod.Package x for x in tojson d
        }
    models.publisher.subscribe = (nv) -> packages nv
    packages models.publisher()

  loadschedulechart = (campaigns,campaign,mod,models) ->
    data = -> []
    campaign.dataloader = (ca)->
      if ca.selected()
        ca.schedulechart.chartcontent data ca
      else
        ca.schedulechart.chartcontent []

    require(["nv.d3"], ->
      data = (campaign) ->
        ret = []
        if campaign.startDate()? && campaign.endDate()
          days = mod.dayrange(campaign.startDate(),20,90)
          now = (new Date()).getTime()
          day = 24*60*60*1000
          maxc = 0
          aus = (ko.unwrap a.id for a in campaign.audiences())
          rd = mod.rangedays(campaign.startDate(),campaign.endDate())
          for pa in models.currentpackages() when pa.id()==(campaign.package()?.id)
            c = pa.count()/(rd.length)
            maxc = maxc+c
            ps = for d in days
              if rd.indexOf(d)>=0
                {x:d,y:c}
              else
                {x:d,y:0}
            ret.push
              key: campaign.name()
              cls: 'campaign'
              type: 'area'
              values: ps
              color: '#a00'
              timeframe: 'days'
        return ret
    )

  loadcampaigns = (mod,models) ->
    campaigns = (nv) ->
      if nv?.id?
        id = nv.id()
        r = routes.controllers.CampaignController.campaignList id
        r.ajax {
          success: (d) ->
            models.campaigns []
            models.campaigns.push new mod.Campaign x for x in tojson d
            models.campaigntablesearchbar?.search()
            for ca in models.campaigns()
              loadschedulechart(models.campaigns(),ca,mod,models)
        }
    models.publisher.subscribe = (nv) -> campaigns nv
    campaigns models.publisher()

  loadaudience = (mod,models) ->
    models.audiencechartdaterange.dataloader = ->
      st = models.audiencechartdaterange.startDate().getTime()
      et = models.audiencechartdaterange.endDate().getTime()
      r = routes.controllers.AudienceController.dashboard(st,et)
      r.ajax {
       success: (d) ->
         models.audiencechart.chartcontent tojson d
      }
    models.audiencechartdaterange.dataloader()

  loadcampaign = (mod,models) ->
    models.campaignchartdaterange.dataloader = ->
      st = models.campaignchartdaterange.startDate().getTime()
      et = models.campaignchartdaterange.endDate().getTime()
      r = routes.controllers.CampaignController.dashboard(st,et)
      r.ajax {
       success: (d) ->
         models.campaignchart.chartcontent tojson d
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