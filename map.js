import React, {Component} from 'react';
import { StyleSheet, Text,Image, View, Platform, TouchableOpacity} from 'react-native';
import MapView, { Marker, Polyline, AnimatedRegion,  ProviderPropType }from 'react-native-maps';
import * as firebase from 'firebase';


export default class map extends React.Component{


  static navigationOptions = {
    title:"",
    headerStyle: {
      backgroundColor: '#696969',
     }
  }


       constructor(props) {
        super(props);
        this.state = {
         latitude: 31.400,
         longitude: 74.2200,
         error: null,
         routeCoordinates: [],
         distanceTravelled: 0,
         prevLatLng: {},
         coordinate: new AnimatedRegion({
          latitude: 31.40,
          longitude: 74.2200,
          latitudeDelta: 0.09,
            longitudeDelta: 0.035
         })
        };
       }



       getMapRegion = () => ({
        latitude: this.state.latitude,
        longitude: this.state.longitude,
        latitudeDelta: 0.09,
        longitudeDelta: 0.035
       })

    
       componentDidMount() {
        navigator.geolocation.getCurrentPosition(
         position => {
           console.log(position);
           this.setState({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
           error: null
          });
        },
        error => this.setState({ error: error.message }),
         { enableHighAccuracy: true, timeout: 20000, maximumAge: 1000 }
         );

         navigator.geolocation.watchPosition(
          position => {
            const { latitude, longitude } = position.coords;
            const {coordinate, routeCoordinates, distanceTravelled } = this.state;
            const newCoordinate = {  latitude,  longitude  };
            this.setState({
             latitude,
             longitude,
              routeCoordinates: routeCoordinates.concat([newCoordinate])
            });
            if (Platform.OS === "android") {
              if (this.marker) {
                this.marker._component.animateMarkerToCoordinate(
                  newCoordinate,
                  500
                );
               }
             } else {
               coordinate.timing(newCoordinate).start();
             }

             this.setState({
              latitude,
              longitude,
               routeCoordinates: routeCoordinates.concat([newCoordinate]),
             });
         },
            error => console.log(error),
            { 
              enableHighAccuracy: true,
              timeout: 20000,
              maximumAge: 1000,
              distanceFilter: 10
            }        
           );  
       }

      
       render() {
        return (
            <View style={styles.container}>
                <MapView style={styles.map}
                        provider={this.props.provider}
                       // provider={PROVIDER_GOOGLE} 
                        region={this.getMapRegion()}>

                <Polyline coordinates={this.state.routeCoordinates} strokeWidth={5} />
                                   
               
                <Marker.Animated
                 ref={marker => {
                 this.marker = marker;
                 }}
                 coordinate={this.state.coordinate}
                 title="Your Current Location">
                         <Image source={require("../assets/b.png")} style={{ height: 35, width: 35 }} />
                </Marker.Animated>

		                                              
      
                <Marker
                coordinate={{latitude: 31.403, longitude: 74.2106}}
                title="COMSATS University, Lahore Campus">
                </Marker>

                <Marker
                coordinate={{latitude: 31.398898, longitude: 74.210966}}
                title="COMSATS, Gate # 2">
                </Marker>

                <Marker
                coordinate={{latitude: 31.401126, longitude: 74.212026}}
                title="Near COMSATS Masjid">
                </Marker>

                <Marker
                coordinate={{latitude: 31.400917, longitude: 74.213726}}
                title="C Block">
                </Marker>

                </MapView>
            
              
            </View>
        );
}
};






const styles = StyleSheet.create({
    container: {
      flex: 1,
      backgroundColor: '#a9a9a9',
      justifyContent: "center",
  
    },
    map: {
        height: '100%'
    }
  });

  