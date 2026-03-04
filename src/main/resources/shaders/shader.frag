#version 330 core

in vec2 fragTex;
in vec3 fragPos;
in vec3 fragNormal;

out vec4 FragColor;

#define MAX_POINT_LIGHTS 10
#define MAX_SPOT_LIGHTS 10

struct Material {
	float specularIntensity;
	float shininess;

};

struct Light {
	vec3 color;
	float ambientIntensity;
	float diffuseIntensity;
};

struct DirectionalLight {
	Light base;
	vec3 dir;
	
};

struct PointLight {
	Light base;
	vec3 position;
	float constant;
	float linear;
	float exponent;
};

struct SpotLight {
	PointLight base;
	vec3 direction;
	float edge;
	
};

uniform sampler2D theTexture;
uniform Material material;


uniform DirectionalLight directionalLight;

uniform int pointLightCount;
uniform PointLight pointLights[MAX_POINT_LIGHTS];

uniform int spotLightCount;
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];

uniform vec3 viewPos;

vec3 CalcDirectionalLight(DirectionalLight dLight, vec3 normal, vec3 fragPos)
{
    vec3 lightDir = normalize(-dLight.dir);
    vec3 viewDir  = normalize(viewPos - fragPos);
    vec3 halfwayDir = normalize(lightDir + viewDir);

    float diff = max(dot(normal, lightDir), 0.0);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), material.shininess);

    vec3 ambient = dLight.base.color * dLight.base.ambientIntensity;
    vec3 diffuse = dLight.base.color * dLight.base.diffuseIntensity * diff;
    vec3 specular = dLight.base.color * material.specularIntensity * spec;

    return ambient + diffuse + specular;
}

vec3 CalcPointLight(PointLight pLight, vec3 normal, vec3 fragPos)
{
    vec3 lightDir = normalize(pLight.position - fragPos);
    vec3 viewDir  = normalize(viewPos - fragPos);
    vec3 halfwayDir = normalize(lightDir + viewDir);

    float diff = max(dot(normal, lightDir), 0.0);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), material.shininess);

    float distance = length(pLight.position - fragPos);
    float attenuation = 1.0 / (pLight.constant +
                               pLight.linear * distance +
                               pLight.exponent * distance * distance);

    vec3 ambient  = pLight.base.color * pLight.base.ambientIntensity;
    vec3 diffuse  = pLight.base.color * pLight.base.diffuseIntensity * diff;
    vec3 specular = pLight.base.color * material.specularIntensity * spec;

    ambient  *= attenuation;
    diffuse  *= attenuation;
    specular *= attenuation;

    return ambient + diffuse + specular;
}

vec3 CalcSpotLight(SpotLight sLight,vec3 normal, vec3 fragPos){

	vec3 lightDir = normalize(sLight.base.position - fragPos);
    vec3 viewDir  = normalize(viewPos - fragPos);
    vec3 halfwayDir = normalize(lightDir + viewDir);

	float diff = max(dot(normal, lightDir), 0.0);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), material.shininess);

    float distance = length(sLight.base.position - fragPos);
    float attenuation = 1.0 / (sLight.base.constant +
                               sLight.base.linear * distance +
                               sLight.base.exponent * distance * distance);

    float theta = dot(lightDir, normalize(-sLight.direction));

    if(theta > sLight.edge)
    {
        vec3 ambient  = sLight.base.base.color * sLight.base.base.ambientIntensity;
        vec3 diffuse  = sLight.base.base.color * sLight.base.base.diffuseIntensity * diff;
        vec3 specular = sLight.base.base.color * material.specularIntensity * spec;

        ambient  *= attenuation;
        diffuse  *= attenuation;
        specular *= attenuation;

        return ambient + diffuse + specular;
    }
    else
    {
        return vec3(0.0);
    }
	
};

void main(){
	
	vec3 norm = normalize(fragNormal);
	vec3 result = vec3(0.);
	
	//Directional Light
	result += CalcDirectionalLight(directionalLight, norm, fragPos);
	
	//Point Light
	for(int i = 0; i<pointLightCount;i++){
		result += CalcPointLight(pointLights[i],norm,fragPos);
	}
	
	//Spot Light 
	for(int i= 0;i< spotLightCount;i++){
		result += CalcSpotLight(spotLights[i],norm,fragPos);
	}
	
	
	vec3 textureColor = texture(theTexture,fragTex).rgb;

	FragColor = vec4(result * textureColor,1.0);

}
