
from stallion import register_endpoints, register_context_hooks, register_request_filters, \
    GET, POST, DELETE, PUT, \
    QueryParam, BodyParam, ObjectParam, DictParam, \
    BaseResource, BaseContextHook, BaseRequestFilter, \
    Roles, StResponse, stallion_context

from io.stallion.users import User

# Test importing from relative files and folders
from example_package.example_module import example_processor
from extra_module import extra_processor
extra_data = {}
extra_data.update(extra_processor())
extra_data.update(example_processor())
assert 'example_request_path' in extra_data
assert 'extra_plugin_folder_path' in extra_data

class MyContextHook(BaseContextHook):
    def hydrate(self, context):
        context['my_jython_context_value'] = 'jabberwocky'
        

class MyRequestFilter(BaseRequestFilter):
    def filter(self, request, response):
        request.user = User().setDisplayName('Alabaster')


class MyPluginResource(BaseResource):
    root_path = "/pages"
    namespaced = True

    @POST("/noop/:key")
    def post_noop(self, key=None):
        return {
            'the_list': ['a', 'b', 'c'],
            'the_key': key,
            'the_dict': {'uno': 1, 'dos': 2}
        }

    
    @GET("/:id")
    def get_post(self, id=None, first_name=None, last_name=None):
        page = stallion_context.dal.pages.filter('id', id).first()
        return page

    @GET("/find-pages", QueryParam("title"), QueryParam("author"), xsrf_validation=False)
    def find_pages(self, id=None, title=None, author=None):
        pages = []
        for page in stallion_context.dal.pages.filter('title', title).filter('author', author).all():
            pages.append(page)
        return pages

    @POST("/postback", BodyParam("first_name", "firstName"), auth=Roles.staff)
    def postback(self, first_name=None):
        return {
            "posted_value": first_name
        }

    @PUT("/putter", DictParam("data"))
    def putter(self, data=None):
        return {
            'your_data': data
        }


register_endpoints(MyPluginResource())
register_context_hooks(MyContextHook())
register_request_filters(MyRequestFilter())


