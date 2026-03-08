#version 330 core

in vec2 fragTex;
in vec3 fragPos;
in vec3 fragNormal;
in vec4 fragPosLightSpace;

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

uniform sampler2D shadowMap;
uniform mat4 lightSpaceMatrix;
uniform float shadowBiasMultiplier;

uniform DirectionalLight directionalLight;

uniform int pointLightCount;
uniform PointLight pointLights[MAX_POINT_LIGHTS];

uniform samplerCube pointShadowMaps[MAX_POINT_LIGHTS];
uniform float farPlane;

uniform int spotLightCount;
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];
uniform sampler2D spotShadowMaps[MAX_SPOT_LIGHTS];
uniform mat4 spotLightSpaceMatrices[MAX_SPOT_LIGHTS];

uniform vec3 viewPos;
uniform int debugMode;

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

vec3 CalcSpotLight(SpotLight sLight, vec3 normal, vec3 fragPos, float shadow){

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

		return ambient + (diffuse + specular) * (1.0 - shadow);
	}
    	else
    	{
        	return vec3(0.0);
    	}
	
};

float CalcShadow(vec4 fragPosLightSpace, vec3 normal, vec3 lightDir){
	vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
	projCoords = projCoords * 0.5 + 0.5;

	// Fragments beyond the light's far plane should NOT be in shadow
	if(projCoords.z > 1.0)
		return 0.0;

	float currentDepth = projCoords.z;

	// Slope-based bias to reduce shadow acne without excessive peter-panning
	float biasBase = shadowBiasMultiplier;
	float bias = biasBase * (1.0 - dot(normal, lightDir));

	// PCF (Percentage Closer Filtering) for softer shadow edges
	float shadow = 0.0;
	vec2 texelSize = 1.0 / textureSize(shadowMap, 0);
	for(int x = -1; x <= 1; x++){
		for(int y = -1; y <= 1; y++){
			float pcfDepth = texture(shadowMap, projCoords.xy + vec2(x, y) * texelSize).r;
			shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;
		}
	}
	shadow /= 9.0;

	return shadow;
}

float CalcSpotShadow(sampler2D sMap, vec4 fragPosLightSpace, vec3 normal, vec3 lightDir){
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;

    if(projCoords.z > 1.0) return 0.0;

    float currentDepth = projCoords.z;
    float bias = shadowBiasMultiplier * (1.0 - dot(normal, lightDir));

    float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(sMap, 0);
    for(int x = -1; x <= 1; x++){
        for(int y = -1; y <= 1; y++){
            float pcfDepth = texture(sMap, projCoords.xy + vec2(x, y) * texelSize).r;
            shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;
        }
    }
    return shadow / 9.0;
}

float CalcPointShadow(samplerCube shadowCube, vec3 fragPos, vec3 lightPos){
    vec3 fragToLight = fragPos - lightPos;
    float currentDepth = length(fragToLight);
    currentDepth = currentDepth / farPlane;
    
    float bias = 0.05;
    float shadow = 0.0;
    
    vec3 sampleOffsetDirections[20] = vec3[](
        vec3(1,1,1), vec3(1,-1,1), vec3(-1,-1,1), vec3(-1,1,1),
        vec3(1,1,-1), vec3(1,-1,-1), vec3(-1,-1,-1), vec3(-1,1,-1),
        vec3(1,1,0), vec3(1,-1,0), vec3(-1,-1,0), vec3(-1,1,0),
        vec3(1,0,1), vec3(-1,0,1), vec3(1,0,-1), vec3(-1,0,-1),
        vec3(0,1,1), vec3(0,-1,1), vec3(0,-1,-1), vec3(0,1,-1)
    );
    
    float diskRadius = 0.05;
    for(int i = 0; i < 20; i++){
        float closestDepth = texture(shadowCube, fragToLight + sampleOffsetDirections[i] * diskRadius).r;
        shadow += currentDepth - bias > closestDepth ? 1.0 : 0.0;
    }
    return shadow / 20.0;
}


void main(){
	
	vec3 norm = normalize(fragNormal);
	vec3 result = vec3(0.);
	vec3 lightDir = normalize(-directionalLight.dir);
	float shadow = CalcShadow(fragPosLightSpace, norm, lightDir);

	
	//Directional Light
	vec3 ambient = directionalLight.base.color * directionalLight.base.ambientIntensity;
	vec3 lighting = CalcDirectionalLight(directionalLight, norm, fragPos);
	result += ambient + (lighting - ambient) * (1.0 - shadow);
	
	//Point Light
	for(int i = 0; i < pointLightCount; i++){
    		float pointShadow = CalcPointShadow(pointShadowMaps[i], fragPos, pointLights[i].position);
    		vec3 ambient = pointLights[i].base.color * pointLights[i].base.ambientIntensity;
    		vec3 lighting = CalcPointLight(pointLights[i], norm, fragPos);
    		result += ambient + (lighting - ambient) * (1.0 - pointShadow);
	}
	
	//Spot Light 
	for(int i = 0; i < spotLightCount; i++){
    		vec3 sLightDir = normalize(spotLights[i].direction);
    		vec4 fragPosSpotLightSpace = spotLightSpaceMatrices[i] * vec4(fragPos, 1.0);
    		float spotShadow = CalcSpotShadow(spotShadowMaps[i], fragPosSpotLightSpace, norm, sLightDir);
    		result += CalcSpotLight(spotLights[i], norm, fragPos, spotShadow);
	}
	
	
	vec3 textureColor = texture(theTexture,fragTex).rgb;

	// Debug mode: visualize shadow value directly
	if (debugMode == 1) {
		// Green = fully lit (shadow=0), Red = fully shadowed (shadow=1)
		// Also show projCoords.xy as blue channel to verify projection
		vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
		projCoords = projCoords * 0.5 + 0.5;
		FragColor = vec4(shadow, 1.0 - shadow, projCoords.z, 1.0);
		return;
	}

	FragColor = vec4(result * textureColor,1.0);

}