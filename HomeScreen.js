import React from 'react';
import { StyleSheet, Text, View, Alert, ImageBackground} from 'react-native';
import { Button } from 'native-base';
import * as firebase from 'firebase';

export default class HomeScreen extends React.Component{

    state={
        email:""
    }

  static navigationOptions = {
    title:"Admin Home",
    headerStyle: {
      backgroundColor: '#696969',
     }
  }
  componentDidMount(){
     this.unsuscribeAuth = firebase.auth().onAuthStateChanged(user=>{
            if(user){
                this.setState({
                email:user.email
            })
            }else{
               this.props.navigation.replace("Login");
            }
            })
  }
  userSignout(){
      firebase.auth().signOut()
      .catch(error=>{
          Alert.alert(error.message)
      })
  }
  componentWillUnmount(){
      this.unsuscribeAuth()
  }
 
    render(){
        return (
            
            <View style={styles.container}>
               
               <Button full rounded danger
                 style={{margin:10,justifyContent:"center",backgroundColor:"#696969"}} 
                 onPress={()=>this.props.navigation.navigate("Map")}>
                    <Text style={{fontSize:22,color:"white"}}>
                         Trucks Location
                    </Text>
                </Button>

                <Button full rounded danger
                 style={{margin:10,justifyContent:"center",backgroundColor:"#696969"}} 
                 onPress={()=>this.props.navigation.navigate("")}>
                    <Text style={{fontSize:22,color:"white"}}>
                         Trash Weight
                    </Text>
                </Button>
            
                <Text style={{textAlign:"center",marginTop:410}}>You are logged in as {this.state.email}</Text>
            
                
                <Button full rounded danger
                 style={{margin:10,justifyContent:"center",backgroundColor:"#696969"}}
                 onPress={()=>this.userSignout()}>
                     <Text style={{fontSize:22,color:"white"}}>
                         Logout
                     </Text>
                 </Button>
            </View>
          );
    }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#a9a9a9',
    justifyContent: "center",

  },
});
